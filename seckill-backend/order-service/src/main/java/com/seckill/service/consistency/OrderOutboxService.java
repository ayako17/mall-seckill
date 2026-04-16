package com.seckill.service.consistency;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.entity.Order;
import com.seckill.entity.OrderOutboxMessage;
import com.seckill.mapper.OrderOutboxMessageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderOutboxService {

    private final OrderOutboxMessageMapper outboxMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.order-paid}")
    private String orderPaidTopic;

    @Value("${app.outbox.max-retry:8}")
    private int maxRetry;

    public OrderOutboxService(OrderOutboxMessageMapper outboxMapper,
                              KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxMapper = outboxMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void createOrderPaidOutbox(Order order, String payload) {
        OrderOutboxMessage msg = new OrderOutboxMessage();
        msg.setMsgKey("ORDER_PAID:" + order.getOrderNo());
        msg.setTopic(orderPaidTopic);
        msg.setPayload(payload);
        msg.setStatus(0);
        msg.setRetryCount(0);
        msg.setNextRetryTime(LocalDateTime.now());
        msg.setCreatedTime(LocalDateTime.now());
        msg.setUpdatedTime(LocalDateTime.now());
        outboxMapper.insert(msg);
    }

    public List<OrderOutboxMessage> loadPendingBatch(int limit) {
        return outboxMapper.selectList(
                new LambdaQueryWrapper<OrderOutboxMessage>()
                        .eq(OrderOutboxMessage::getStatus, 0)
                        .le(OrderOutboxMessage::getNextRetryTime, LocalDateTime.now())
                        .orderByAsc(OrderOutboxMessage::getCreatedTime)
                        .last("LIMIT " + limit)
        );
    }

    public void markSent(Long id) {
        outboxMapper.update(null,
                new LambdaUpdateWrapper<OrderOutboxMessage>()
                        .eq(OrderOutboxMessage::getId, id)
                        .set(OrderOutboxMessage::getStatus, 1)
                        .set(OrderOutboxMessage::getUpdatedTime, LocalDateTime.now())
        );
    }

    public void markRetry(Long id, int currentRetry, String reason) {
        int retry = currentRetry + 1;
        int status = retry >= maxRetry ? 2 : 0;
        outboxMapper.update(null,
                new LambdaUpdateWrapper<OrderOutboxMessage>()
                        .eq(OrderOutboxMessage::getId, id)
                        .set(OrderOutboxMessage::getRetryCount, retry)
                        .set(OrderOutboxMessage::getStatus, status)
                        .set(OrderOutboxMessage::getNextRetryTime, LocalDateTime.now().plusSeconds((long) Math.min(120, retry * 5)))
                        .set(OrderOutboxMessage::getUpdatedTime, LocalDateTime.now())
        );
    }

    public void send(OrderOutboxMessage message) {
        kafkaTemplate.send(message.getTopic(), message.getMsgKey(), message.getPayload());
    }
}


