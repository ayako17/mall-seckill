package com.seckill.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryReserveEvent {

    private String traceId;

    private String orderNo;

    private Long userId;

    private Long productId;

    private Long seckillId;

    private Integer quantity;

    private LocalDateTime createdAt;
}

