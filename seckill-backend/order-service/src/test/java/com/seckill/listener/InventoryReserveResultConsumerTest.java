package com.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.event.InventoryReserveResultEvent;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.consistency.RedisPreDeductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReserveResultConsumerTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RedisPreDeductService redisPreDeductService;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    void shouldCancelOrderAndRollbackWhenReserveFailed() {
        InventoryReserveResultConsumer consumer = new InventoryReserveResultConsumer(orderMapper, redisPreDeductService);

        InventoryReserveResultEvent event = new InventoryReserveResultEvent();
        event.setOrderNo("MS202604160001");
        event.setUserId(10L);
        event.setProductId(100L);
        event.setSeckillId(200L);
        event.setQuantity(1);
        event.setSuccess(false);
        event.setReason("inventory not enough");

        when(orderMapper.markReserveFailed("MS202604160001")).thenReturn(1);

        consumer.consumeReserveResult(JSON.toJSONString(event), acknowledgment);

        verify(orderMapper).markReserveFailed("MS202604160001");
        verify(redisPreDeductService).rollbackStock(200L, 10L, 1);
        verify(redisPreDeductService).setOrderResult("MS202604160001", "inventory not enough", 10);
        verify(acknowledgment).acknowledge();
    }
}