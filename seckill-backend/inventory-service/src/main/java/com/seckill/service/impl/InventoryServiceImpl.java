package com.seckill.service.impl;

import com.seckill.mapper.InventoryMapper;
import com.seckill.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserveStock(Long productId, Integer quantity) {
        int rows = inventoryMapper.reserve(productId, quantity);
        if (rows == 0) {
            throw new RuntimeException("reserve stock failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDeduct(Long productId, Integer quantity) {
        int rows = inventoryMapper.confirm(productId, quantity);
        if (rows == 0) {
            throw new RuntimeException("confirm stock failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseReserved(Long productId, Integer quantity) {
        int rows = inventoryMapper.release(productId, quantity);
        if (rows == 0) {
            throw new RuntimeException("release stock failed");
        }
    }
}


