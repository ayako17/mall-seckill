package com.seckill.service.impl;

import com.seckill.mapper.InventoryMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void reserveShouldThrowWhenNoStock() {
        when(inventoryMapper.reserve(100L, 1)).thenReturn(0);
        Assertions.assertThrows(RuntimeException.class, () -> inventoryService.reserveStock(100L, 1));
    }

    @Test
    void confirmShouldThrowWhenNoLockedStock() {
        when(inventoryMapper.confirm(100L, 1)).thenReturn(0);
        Assertions.assertThrows(RuntimeException.class, () -> inventoryService.confirmDeduct(100L, 1));
    }
}

