package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.entity.Order;
import com.seckill.mapper.OrderMapper;
import com.seckill.mapper.SeckillProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SeckillOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderConsumer.class);

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String ORDER_RESULT_KEY = "seckill:order:result:";
    private static final String SECKILL_ORDER_PENDING_KEY = "seckill:order:pending:";
    private static final String SECKILL_ORDER_SUCCESS_USERS_KEY = "seckill:order:success:";

    public SeckillOrderConsumer(OrderMapper orderMapper,
                                SeckillProductMapper seckillProductMapper,
                                StringRedisTemplate redisTemplate) {
        this.orderMapper = orderMapper;
        this.seckillProductMapper = seckillProductMapper;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "seckill-order", groupId = "order-service-group")
    public void consumeOrder(String message, Acknowledgment ack) {
        log.info("收到秒杀订单消息: {}", message);

        Order order = JSON.parseObject(message, Order.class);
        boolean success = false;
        String errorMsg = "";

        String stockKey = SECKILL_STOCK_KEY + order.getSeckillId();
        String pendingKey = SECKILL_ORDER_PENDING_KEY + order.getUserId() + ":" + order.getSeckillId();
        String successUsersKey = SECKILL_ORDER_SUCCESS_USERS_KEY + order.getSeckillId();
        int quantity = order.getQuantity() == null ? 1 : order.getQuantity();
        boolean redisDecremented = false;

        try {
            Long remainStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
            if (remainStock == null || remainStock < 0) {
                if (remainStock != null) {
                    redisTemplate.opsForValue().increment(stockKey, quantity);
                }
                log.warn("[Redis库存不足] seckillId={}, orderNo={}, remainAfterDecr={}",
                        order.getSeckillId(), order.getOrderNo(), remainStock);
                errorMsg = "库存不足";
                return;
            }
            redisDecremented = true;

            int rows = seckillProductMapper.deductStockDirect(order.getSeckillId(), quantity);
            if (rows == 0) {
                redisTemplate.opsForValue().increment(stockKey, quantity);
                redisDecremented = false;
                log.error("[DB库存不足] Redis扣减成功但DB扣减失败，已回滚Redis: seckillId={}, orderNo={}",
                        order.getSeckillId(), order.getOrderNo());
                errorMsg = "库存同步异常，请稍后重试";
                return;
            }

            try {
                orderMapper.insert(order);
                success = true;
                log.info("[订单创建成功] orderNo={}, seckillId={}, userId={}",
                        order.getOrderNo(), order.getSeckillId(), order.getUserId());
            } catch (DuplicateKeyException e) {
                success = true;
                log.warn("[幂等保护] 订单已存在，跳过重复消费: orderNo={}", order.getOrderNo());
            }

        } catch (Exception e) {
            log.error("[订单处理异常] orderNo={}", order.getOrderNo(), e);
            errorMsg = e.getMessage() != null ? e.getMessage() : "订单处理异常";
            if (redisDecremented) {
                redisTemplate.opsForValue().increment(stockKey, quantity);
                log.warn("[Redis回滚] seckillId={}, quantity={}", order.getSeckillId(), quantity);
            }
        } finally {
            String resultKey = ORDER_RESULT_KEY + order.getOrderNo();
            if (success) {
                redisTemplate.opsForSet().add(successUsersKey, String.valueOf(order.getUserId()));
                redisTemplate.opsForValue().set(resultKey, "SUCCESS", 10, TimeUnit.MINUTES);
            } else if (!errorMsg.isEmpty()) {
                redisTemplate.opsForValue().set(resultKey, errorMsg, 10, TimeUnit.MINUTES);
            }

            // Release pending lock in all cases.
            // Success relies on successUsersKey to reject duplicate requests.
            redisTemplate.delete(pendingKey);
            ack.acknowledge();
        }
    }
}
