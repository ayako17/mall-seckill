package com.seckill.client;

import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryTccFeignFallback implements InventoryTccFeignClient {

    @Override
    public TccResponse tryReserve(TccOrderRequest request) {
        return TccResponse.fail("inventory-service unavailable in try phase");
    }

    @Override
    public TccResponse confirm(String xid) {
        return TccResponse.fail("inventory-service unavailable in confirm phase");
    }

    @Override
    public TccResponse cancel(String xid) {
        return TccResponse.fail("inventory-service unavailable in cancel phase");
    }
}