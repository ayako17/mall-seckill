package com.seckill.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderPaidEvent {

    private String orderNo;

    private Long userId;

    private Long productId;

    private Long seckillId;

    private Integer quantity;

    private String transactionId;

    private Integer paymentMethod;

    private LocalDateTime paidTime;
}


