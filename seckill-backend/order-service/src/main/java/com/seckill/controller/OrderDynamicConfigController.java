package com.seckill.controller;

import com.seckill.config.dynamic.SeckillDynamicControlProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/config")
@RefreshScope
public class OrderDynamicConfigController {

    private final SeckillDynamicControlProperties dynamicControlProperties;

    public OrderDynamicConfigController(SeckillDynamicControlProperties dynamicControlProperties) {
        this.dynamicControlProperties = dynamicControlProperties;
    }

    @GetMapping("/seckill-control")
    public Map<String, Object> showSeckillControl() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", dynamicControlProperties.isEnabled());
        result.put("requestThresholdPerSecond", dynamicControlProperties.getRequestThresholdPerSecond());
        result.put("tip", "Update value in Nacos and trigger /actuator/refresh to take effect immediately");
        return result;
    }
}
