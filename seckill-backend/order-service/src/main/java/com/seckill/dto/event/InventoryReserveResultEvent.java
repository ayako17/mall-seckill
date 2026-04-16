package com.seckill.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryReserveResultEvent {

    private String traceId;

    private String orderNo;

    private Long userId;

    private Long productId;

    private Long seckillId;

    private Integer quantity;

    private boolean success;

    private String reason;

    private LocalDateTime handledAt;
}

