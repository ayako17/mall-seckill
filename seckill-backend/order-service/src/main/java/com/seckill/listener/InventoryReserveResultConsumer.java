package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.InventoryReserveResultEvent;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.consistency.RedisPreDeductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class InventoryReserveResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryReserveResultConsumer.class);

    private final OrderMapper orderMapper;
    private final RedisPreDeductService redisPreDeductService;

    public InventoryReserveResultConsumer(OrderMapper orderMapper,
                                          RedisPreDeductService redisPreDeductService) {
        this.orderMapper = orderMapper;
        this.redisPreDeductService = redisPreDeductService;
    }

    @KafkaListener(topics = "${app.kafka.topic.inventory-reserve-result}", groupId = "${spring.kafka.consumer.group-id}-reserve-result")
    public void consumeReserveResult(String message, Acknowledgment ack) {
        InventoryReserveResultEvent result = JSON.parseObject(message, InventoryReserveResultEvent.class);
        try {
            if (result.isSuccess()) {
                redisPreDeductService.markOrderSuccess(result.getSeckillId(), result.getUserId());
                redisPreDeductService.setOrderResult(result.getOrderNo(), "SUCCESS", 10);
                log.info("inventory reserve success, orderNo={}", result.getOrderNo());
            } else {
                int changed = orderMapper.markReserveFailed(result.getOrderNo());
                if (changed > 0) {
                    redisPreDeductService.rollbackStock(result.getSeckillId(), result.getUserId(), result.getQuantity());
                }
                String reason = result.getReason() == null ? "库存预留失败" : result.getReason();
                redisPreDeductService.setOrderResult(result.getOrderNo(), reason, 10);
                log.warn("inventory reserve failed, orderNo={}, reason={}", result.getOrderNo(), reason);
            }
        } finally {
            ack.acknowledge();
        }
    }
}