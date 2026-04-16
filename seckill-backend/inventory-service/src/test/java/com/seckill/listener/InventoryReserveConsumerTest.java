package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.InventoryReserveEvent;
import com.seckill.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReserveConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private RBucket<String> bucket;

    @Test
    void shouldReserveAndPublishSuccessResult() {
        InventoryReserveConsumer consumer = new InventoryReserveConsumer(inventoryService, redissonClient, kafkaTemplate);
        ReflectionTestUtils.setField(consumer, "reserveResultTopic", "inventory.reserve.result");

        when(redissonClient.getBucket(anyString(), any())).thenAnswer(invocation -> bucket);
        when(bucket.trySet(eq("1"), eq(3L), any())).thenReturn(true);

        InventoryReserveEvent event = new InventoryReserveEvent();
        event.setTraceId("trace-1");
        event.setOrderNo("MS202604160001");
        event.setUserId(10L);
        event.setProductId(100L);
        event.setSeckillId(200L);
        event.setQuantity(1);

        consumer.onReserveRequest(JSON.toJSONString(event));

        verify(inventoryService).reserveStock(100L, 1);
        verify(kafkaTemplate).send(eq("inventory.reserve.result"), eq("MS202604160001"), contains("\"success\":true"));
    }
}
