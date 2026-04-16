package com.seckill.service;

public interface InventoryService {

    void reserveStock(Long productId, Integer quantity);

    void confirmDeduct(Long productId, Integer quantity);

    void releaseReserved(Long productId, Integer quantity);
}


