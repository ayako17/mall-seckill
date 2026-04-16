package com.seckill.client;

import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", path = "/api/tcc/inventory", fallback = InventoryTccFeignFallback.class)
public interface InventoryTccFeignClient {

    @PostMapping("/try")
    TccResponse tryReserve(@RequestBody TccOrderRequest request);

    @PostMapping("/confirm/{xid}")
    TccResponse confirm(@PathVariable("xid") String xid);

    @PostMapping("/cancel/{xid}")
    TccResponse cancel(@PathVariable("xid") String xid);
}