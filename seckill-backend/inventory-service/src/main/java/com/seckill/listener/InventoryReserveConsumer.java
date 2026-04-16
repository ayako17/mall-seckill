package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.InventoryReserveEvent;
import com.seckill.dto.event.InventoryReserveResultEvent;
import com.seckill.service.InventoryService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class InventoryReserveConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryReserveConsumer.class);
    private static final String RESERVE_CONSUMED_KEY = "inventory:reserve:consumed:";

    private final InventoryService inventoryService;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.inventory-reserve-result}")
    private String reserveResultTopic;

    public InventoryReserveConsumer(InventoryService inventoryService,
                                    RedissonClient redissonClient,
                                    KafkaTemplate<String, String> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.redissonClient = redissonClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topic.inventory-reserve}", groupId = "${spring.kafka.consumer.group-id}-reserve")
    public void onReserveRequest(String message) {
        InventoryReserveEvent event = JSON.parseObject(message, InventoryReserveEvent.class);
        String dedupeKey = RESERVE_CONSUMED_KEY + event.getOrderNo();
        RBucket<String> dedupe = redissonClient.getBucket(dedupeKey, StringCodec.INSTANCE);
        if (!dedupe.trySet("1", 3, TimeUnit.DAYS)) {
            return;
        }

        InventoryReserveResultEvent result = new InventoryReserveResultEvent();
        result.setTraceId(event.getTraceId());
        result.setOrderNo(event.getOrderNo());
        result.setUserId(event.getUserId());
        result.setProductId(event.getProductId());
        result.setSeckillId(event.getSeckillId());
        result.setQuantity(event.getQuantity());
        result.setHandledAt(LocalDateTime.now());

        try {
            inventoryService.reserveStock(event.getProductId(), event.getQuantity());
            result.setSuccess(true);
            result.setReason("OK");
            log.info("inventory reserve success, orderNo={}", event.getOrderNo());
        } catch (Exception ex) {
            result.setSuccess(false);
            result.setReason(ex.getMessage() == null ? "reserve stock failed" : ex.getMessage());
            log.warn("inventory reserve failed, orderNo={}, reason={}", event.getOrderNo(), result.getReason());
        }

        kafkaTemplate.send(reserveResultTopic, event.getOrderNo(), JSON.toJSONString(result));
    }
}