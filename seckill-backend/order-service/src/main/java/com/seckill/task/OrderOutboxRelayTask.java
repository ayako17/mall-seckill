package com.seckill.task;

import com.seckill.entity.OrderOutboxMessage;
import com.seckill.service.consistency.OrderOutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderOutboxRelayTask {

    private static final Logger log = LoggerFactory.getLogger(OrderOutboxRelayTask.class);

    private final OrderOutboxService outboxService;

    public OrderOutboxRelayTask(OrderOutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.scan-interval-ms:5000}")
    public void relay() {
        List<OrderOutboxMessage> batch = outboxService.loadPendingBatch(100);
        for (OrderOutboxMessage msg : batch) {
            try {
                outboxService.send(msg);
                outboxService.markSent(msg.getId());
            } catch (Exception e) {
                log.error("outbox send failed, id={}", msg.getId(), e);
                outboxService.markRetry(msg.getId(), msg.getRetryCount(), e.getMessage());
            }
        }
    }
}


