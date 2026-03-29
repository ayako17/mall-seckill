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

/**
 * 秒杀订单 Kafka 消费者
 *
 * <p><b>架构设计（双重保障）：</b>
 * <pre>
 * 用户请求
 *   │
 *   ▼
 * OrderServiceImpl.createSeckillOrder()
 *   ├─ 幂等校验（Redis SETNX）
 *   └─ 直接写入 Kafka（不扣减库存，快速返回排队中）
 *         │
 *         ▼
 *   SeckillOrderConsumer（异步）
 *   ├─ Step 1: Redis DECR —— 第一道门禁（原子扣减，防超卖）
 *   │          返回 < 0 → 回滚 Redis 并拒绝，不碰 DB
 *   ├─ Step 2: DB UPDATE —— 第二道门禁（SQL 条件保底，防 DB 负库存）
 *   │          返回 0   → 极端不一致告警，回滚 Redis
 *   └─ Step 3: 创建订单记录（唯一键幂等保护）
 * </pre>
 *
 * <p><b>优势：</b>
 * <ol>
 *   <li>Redis 是真实剩余库存的权威来源，前端实时展示准确。
 *   <li>99% 的超卖请求在 Redis DECR 即被拒绝，DB 行锁压力极小。
 *   <li>DB 的 {@code available_stock >= quantity} SQL 条件是最后一道安全网。
 *   <li>Kafka 消费幂等：唯一键重复时安全跳过。
 * </ol>
 */
@Component
public class SeckillOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderConsumer.class);

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String ORDER_RESULT_KEY  = "seckill:order:result:";

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
        int quantity = order.getQuantity() == null ? 1 : order.getQuantity();
        boolean redisDecremented = false;

        try {
            // ── Step 1: Redis 原子扣减 —— 第一道门禁 ─────────────────────────────
            // DECR 是原子操作，高并发下只有库存充足的请求能通过。
            // 这使 Redis 成为剩余库存的权威来源，前端可实时读取展示。
            Long remainStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
            if (remainStock == null || remainStock < 0) {
                // 库存不足：回滚本次 DECR，拒绝该消息，不碰 DB
                if (remainStock != null) {
                    redisTemplate.opsForValue().increment(stockKey, quantity);
                }
                log.warn("[Redis 库存不足] 拒绝订单: seckillId={}, orderNo={}, remainAfterDecr={}",
                        order.getSeckillId(), order.getOrderNo(), remainStock);
                errorMsg = "库存不足";
                // 直接进入 finally 提交 offset（消息本身没有问题，是正常的超卖拦截）
                return;
            }
            redisDecremented = true;
            log.debug("[Redis 扣减成功] seckillId={}, 剩余库存={}", order.getSeckillId(), remainStock);

            // ── Step 2: 同步 DB 库存 —— 第二道安全网 ────────────────────────────
            // 使用无 version 校验的直接扣减，避免乐观锁重试风暴。
            // SQL 内置 available_stock >= quantity 条件，防止 DB 出现负库存。
            int rows = seckillProductMapper.deductStockDirect(
                    order.getSeckillId(),
                    quantity
            );

            if (rows == 0) {
                // 极端情况：Redis 有库存但 DB 没有
                // （例：Redis 重启后预热数据错误，或 DB 被手动修改）
                // 回滚 Redis 扣减，保持 Redis 与 DB 的一致性
                redisTemplate.opsForValue().increment(stockKey, quantity);
                redisDecremented = false;
                log.error("[DB 库存不足 - 数据不一致告警] Redis 扣减成功但 DB 扣减失败！" +
                          "已回滚 Redis。seckillId={}, orderNo={}。请立即核查库存数据！",
                        order.getSeckillId(), order.getOrderNo());
                errorMsg = "库存同步异常，请稍后重试";
                return;
            }

            // ── Step 3: 创建订单记录（唯一键保证幂等）────────────────────────────
            try {
                orderMapper.insert(order);
                success = true;
                log.info("[订单创建成功] orderNo={}, seckillId={}, userId={}",
                        order.getOrderNo(), order.getSeckillId(), order.getUserId());
            } catch (DuplicateKeyException e) {
                // 唯一键冲突：Kafka at-least-once 重复投递，安全跳过
                log.warn("[幂等保护] 订单已存在，跳过重复消费: orderNo={}", order.getOrderNo());
                success = true;
            }

        } catch (Exception e) {
            log.error("[订单处理异常] orderNo={}", order.getOrderNo(), e);
            errorMsg = e.getMessage() != null ? e.getMessage() : "订单处理异常";
            // 如果 Redis 已扣减但后续步骤异常，回滚 Redis 保持一致性
            if (redisDecremented) {
                redisTemplate.opsForValue().increment(stockKey, quantity);
                log.warn("[Redis 回滚] 因异常已回滚 Redis 库存: seckillId={}, quantity={}",
                        order.getSeckillId(), quantity);
            }
        } finally {
            // ── Step 4: 回写订单结果到 Redis，供前端轮询 ─────────────────────────
            String resultKey = ORDER_RESULT_KEY + order.getOrderNo();
            if (success) {
                redisTemplate.opsForValue().set(resultKey, "SUCCESS", 10, TimeUnit.MINUTES);
            } else if (!errorMsg.isEmpty()) {
                redisTemplate.opsForValue().set(resultKey, errorMsg, 10, TimeUnit.MINUTES);
            }
            // ── Step 5: 手动提交 offset ───────────────────────────────────────────
            ack.acknowledge();
        }
    }
}
