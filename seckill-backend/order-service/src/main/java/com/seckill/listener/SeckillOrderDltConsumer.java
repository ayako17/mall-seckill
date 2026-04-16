package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.OrderCreateFailEvent;
import com.seckill.service.consistency.RedisPreDeductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class SeckillOrderDltConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderDltConsumer.class);

    private final RedisPreDeductService redisPreDeductService;

    public SeckillOrderDltConsumer(RedisPreDeductService redisPreDeductService) {
        this.redisPreDeductService = redisPreDeductService;
    }

    @KafkaListener(topics = "${app.kafka.topic.seckill-order-dlt}", groupId = "${spring.kafka.consumer.group-id}-dlt")
    public void consumeDlt(String message, Acknowledgment ack) {
        OrderCreateFailEvent failEvent = JSON.parseObject(message, OrderCreateFailEvent.class);

        try {
            redisPreDeductService.rollbackStock(
                    failEvent.getSeckillId(),
                    failEvent.getUserId(),
                    failEvent.getQuantity() == null ? 1 : failEvent.getQuantity()
            );
            log.warn("DLT compensation done, orderNo={}, reason={}", failEvent.getOrderNo(), failEvent.getReason());
        } catch (Exception e) {
            log.error("DLT compensation failed again, orderNo={}", failEvent.getOrderNo(), e);
        } finally {
            ack.acknowledge();
        }
    }
}