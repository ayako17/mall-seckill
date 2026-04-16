package com.seckill.dto.event;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillOrderEvent {

    private String traceId;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long productId;

    private Long seckillId;

    private Integer quantity;

    private BigDecimal payAmount;

    private LocalDateTime createTime;

    private LocalDateTime expireTime;
}


