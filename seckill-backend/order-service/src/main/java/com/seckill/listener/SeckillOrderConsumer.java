package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.InventoryReserveEvent;
import com.seckill.dto.event.OrderCreateFailEvent;
import com.seckill.dto.event.SeckillOrderEvent;
import com.seckill.entity.Order;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.consistency.RedisPreDeductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SeckillOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderConsumer.class);

    private final OrderMapper orderMapper;
    private final RedisPreDeductService redisPreDeductService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.seckill-order-dlt}")
    private String orderDltTopic;

    @Value("${app.kafka.topic.inventory-reserve}")
    private String inventoryReserveTopic;

    public SeckillOrderConsumer(OrderMapper orderMapper,
                                RedisPreDeductService redisPreDeductService,
                                KafkaTemplate<String, String> kafkaTemplate) {
        this.orderMapper = orderMapper;
        this.redisPreDeductService = redisPreDeductService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topic.seckill-order-create}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrder(String message, Acknowledgment ack) {
        SeckillOrderEvent event = JSON.parseObject(message, SeckillOrderEvent.class);
        boolean orderCreated = false;
        String errorMsg = "";

        try {
            Order order = new Order();
            order.setId(event.getOrderId());
            order.setOrderNo(event.getOrderNo());
            order.setUserId(event.getUserId());
            order.setProductId(event.getProductId());
            order.setSeckillId(event.getSeckillId());
            order.setOrderType(1);
            order.setPayAmount(event.getPayAmount());
            order.setQuantity(event.getQuantity());
            order.setStatus(0);
            order.setCreateTime(event.getCreateTime());
            order.setExpireTime(event.getExpireTime());

            orderMapper.insert(order);
            orderCreated = true;
            log.info("order created, orderNo={}", event.getOrderNo());
        } catch (DuplicateKeyException dup) {
            orderCreated = true;
            log.warn("duplicate order consume ignored, orderNo={}", event.getOrderNo());
        } catch (Exception e) {
            errorMsg = e.getMessage() == null ? "创建订单异常" : e.getMessage();
            log.error("consume create-order failed, orderNo={}", event.getOrderNo(), e);
            redisPreDeductService.rollbackStock(event.getSeckillId(), event.getUserId(), event.getQuantity());
            publishDlt(message, event, errorMsg);
        } finally {
            if (orderCreated) {
                publishReserveEvent(event);
                redisPreDeductService.setOrderResult(event.getOrderNo(), "RESERVING", 10);
            } else if (!errorMsg.isEmpty()) {
                redisPreDeductService.setOrderResult(event.getOrderNo(), errorMsg, 10);
            }
            ack.acknowledge();
        }
    }

    private void publishDlt(String rawMessage, SeckillOrderEvent event, String reason) {
        OrderCreateFailEvent failEvent = new OrderCreateFailEvent();
        failEvent.setOrderNo(event.getOrderNo());
        failEvent.setSeckillId(event.getSeckillId());
        failEvent.setUserId(event.getUserId());
        failEvent.setQuantity(event.getQuantity());
        failEvent.setReason(reason);
        failEvent.setFailedAt(LocalDateTime.now());
        failEvent.setRawMessage(rawMessage);
        kafkaTemplate.send(orderDltTopic, event.getOrderNo(), JSON.toJSONString(failEvent));
    }

    private void publishReserveEvent(SeckillOrderEvent orderEvent) {
        InventoryReserveEvent reserveEvent = new InventoryReserveEvent();
        reserveEvent.setTraceId(orderEvent.getTraceId());
        reserveEvent.setOrderNo(orderEvent.getOrderNo());
        reserveEvent.setUserId(orderEvent.getUserId());
        reserveEvent.setProductId(orderEvent.getProductId());
        reserveEvent.setSeckillId(orderEvent.getSeckillId());
        reserveEvent.setQuantity(orderEvent.getQuantity());
        reserveEvent.setCreatedAt(LocalDateTime.now());
        kafkaTemplate.send(inventoryReserveTopic, orderEvent.getOrderNo(), JSON.toJSONString(reserveEvent));
    }
}