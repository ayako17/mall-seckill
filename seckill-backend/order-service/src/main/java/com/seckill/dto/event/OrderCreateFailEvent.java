package com.seckill.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateFailEvent {

    private String orderNo;

    private Long seckillId;

    private Long userId;

    private Integer quantity;

    private String reason;

    private LocalDateTime failedAt;

    private String rawMessage;
}


