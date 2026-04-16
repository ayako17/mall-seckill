package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.OrderPaidEvent;
import com.seckill.service.InventoryService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class OrderPaidConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPaidConsumer.class);
    private static final String ORDER_PAID_CONSUMED_KEY = "inventory:paid:consumed:";

    private final InventoryService inventoryService;
    private final RedissonClient redissonClient;

    public OrderPaidConsumer(InventoryService inventoryService,
                             RedissonClient redissonClient) {
        this.inventoryService = inventoryService;
        this.redissonClient = redissonClient;
    }

    @KafkaListener(topics = "${app.kafka.topic.order-paid}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderPaid(String message) {
        OrderPaidEvent event = JSON.parseObject(message, OrderPaidEvent.class);
        String dedupeKey = ORDER_PAID_CONSUMED_KEY + event.getOrderNo();

        RBucket<String> dedupe = redissonClient.getBucket(dedupeKey, StringCodec.INSTANCE);
        if (!dedupe.trySet("1", Duration.ofDays(3).toSeconds(), TimeUnit.SECONDS)) {
            return;
        }

        try {
            inventoryService.confirmDeduct(event.getProductId(), event.getQuantity());
            log.info("order paid inventory confirmed, orderNo={}", event.getOrderNo());
        } catch (Exception ex) {
            dedupe.delete();
            throw ex;
        }
    }
}


