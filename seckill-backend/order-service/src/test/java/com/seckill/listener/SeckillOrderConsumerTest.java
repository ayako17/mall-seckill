package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.SeckillOrderEvent;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.consistency.RedisPreDeductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeckillOrderConsumerTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RedisPreDeductService redisPreDeductService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    void shouldRollbackAndSendDltWhenCreateOrderFail() {
        SeckillOrderConsumer consumer = new SeckillOrderConsumer(orderMapper, redisPreDeductService, kafkaTemplate);
        ReflectionTestUtils.setField(consumer, "orderDltTopic", "seckill.order.create.dlt");

        SeckillOrderEvent event = new SeckillOrderEvent();
        event.setOrderId(1001L);
        event.setOrderNo("MS202604140001");
        event.setUserId(10L);
        event.setProductId(100L);
        event.setSeckillId(200L);
        event.setQuantity(1);
        event.setPayAmount(BigDecimal.TEN);
        event.setCreateTime(LocalDateTime.now());
        event.setExpireTime(LocalDateTime.now().plusMinutes(15));

        when(orderMapper.insert(ArgumentMatchers.any())).thenThrow(new RuntimeException("db error"));

        consumer.consumeOrder(JSON.toJSONString(event), acknowledgment);

        verify(redisPreDeductService).rollbackStock(200L, 10L, 1);
        verify(redisPreDeductService).setOrderResult(eq("MS202604140001"), contains("db error"), eq(10L));
        verify(kafkaTemplate).send(eq("seckill.order.create.dlt"), eq("MS202604140001"), anyString());
        verify(acknowledgment).acknowledge();
    }
}

