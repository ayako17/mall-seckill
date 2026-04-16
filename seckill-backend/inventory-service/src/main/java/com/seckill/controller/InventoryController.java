package com.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.entity.Inventory;
import com.seckill.mapper.InventoryMapper;
import com.seckill.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    public InventoryController(InventoryService inventoryService,
                               InventoryMapper inventoryMapper) {
        this.inventoryService = inventoryService;
        this.inventoryMapper = inventoryMapper;
    }

    @PostMapping("/reserve")
    public String reserve(@RequestParam Long productId, @RequestParam Integer quantity) {
        try {
            inventoryService.reserveStock(productId, quantity);
            return "OK";
        } catch (Exception ex) {
            return "库存服务繁忙，请稍后重试";
        }
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam Long productId, @RequestParam Integer quantity) {
        try {
            inventoryService.confirmDeduct(productId, quantity);
            return "OK";
        } catch (Exception ex) {
            return "库存服务繁忙，请稍后重试";
        }
    }

    @PostMapping("/release")
    public String release(@RequestParam Long productId, @RequestParam Integer quantity) {
        try {
            inventoryService.releaseReserved(productId, quantity);
            return "OK";
        } catch (Exception ex) {
            return "库存服务繁忙，请稍后重试";
        }
    }

    @GetMapping("/check/{productId}")
    @SentinelResource(value = "inventoryCheckApi", fallback = "checkFallback")
    public Map<String, Object> check(@PathVariable Long productId) {
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getProductId, productId)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("available", inventory != null && inventory.getAvailableStock() > 0);
        result.put("availableStock", inventory == null ? 0 : inventory.getAvailableStock());
        result.put("degraded", false);
        return result;
    }

    public Map<String, Object> checkFallback(Long productId, Throwable ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("available", true);
        result.put("availableStock", -1);
        result.put("degraded", true);
        result.put("message", "库存系统降级中，已返回兜底结果");
        return result;
    }
}