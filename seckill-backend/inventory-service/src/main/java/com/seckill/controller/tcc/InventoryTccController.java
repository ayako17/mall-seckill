package com.seckill.controller.tcc;

import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;
import com.seckill.service.InventoryTccService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tcc/inventory")
public class InventoryTccController {

    private final InventoryTccService inventoryTccService;

    public InventoryTccController(InventoryTccService inventoryTccService) {
        this.inventoryTccService = inventoryTccService;
    }

    @PostMapping("/try")
    public TccResponse tryReserve(@RequestBody TccOrderRequest request) {
        return inventoryTccService.tryReserve(request);
    }

    @PostMapping("/confirm/{xid}")
    public TccResponse confirm(@PathVariable String xid) {
        return inventoryTccService.confirm(xid);
    }

    @PostMapping("/cancel/{xid}")
    public TccResponse cancel(@PathVariable String xid) {
        return inventoryTccService.cancel(xid);
    }
}


