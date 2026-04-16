package com.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;
import com.seckill.service.InventoryService;
import com.seckill.service.InventoryTccService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class InventoryTccServiceImpl implements InventoryTccService {

    private static final String INVENTORY_TCC_TRY_KEY = "tcc:inventory:try:";

    private final StringRedisTemplate redisTemplate;
    private final InventoryService inventoryService;

    public InventoryTccServiceImpl(StringRedisTemplate redisTemplate,
                                   InventoryService inventoryService) {
        this.redisTemplate = redisTemplate;
        this.inventoryService = inventoryService;
    }

    @Override
    public TccResponse tryReserve(TccOrderRequest request) {
        String key = INVENTORY_TCC_TRY_KEY + request.getXid();
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, JSON.toJSONString(request), 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(ok)) {
            return TccResponse.fail("inventory try duplicated");
        }
        inventoryService.reserveStock(request.getProductId(), request.getQuantity());
        return TccResponse.ok("inventory try success");
    }

    @Override
    public TccResponse confirm(String xid) {
        String key = INVENTORY_TCC_TRY_KEY + xid;
        String payload = redisTemplate.opsForValue().get(key);
        if (payload == null) {
            return TccResponse.ok("inventory confirm idempotent");
        }

        TccOrderRequest req = JSON.parseObject(payload, TccOrderRequest.class);
        inventoryService.confirmDeduct(req.getProductId(), req.getQuantity());
        redisTemplate.delete(key);
        return TccResponse.ok("inventory confirm success");
    }

    @Override
    public TccResponse cancel(String xid) {
        String key = INVENTORY_TCC_TRY_KEY + xid;
        String payload = redisTemplate.opsForValue().get(key);
        if (payload != null) {
            TccOrderRequest req = JSON.parseObject(payload, TccOrderRequest.class);
            inventoryService.releaseReserved(req.getProductId(), req.getQuantity());
            redisTemplate.delete(key);
        }
        return TccResponse.ok("inventory cancel success");
    }
}


