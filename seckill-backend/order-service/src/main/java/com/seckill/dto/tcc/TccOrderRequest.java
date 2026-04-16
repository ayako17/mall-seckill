package com.seckill.dto.tcc;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TccOrderRequest {

    private String xid;

    private Long userId;

    private Long productId;

    private Long seckillId;

    private Integer quantity;

    private BigDecimal payAmount;

    private String orderNo;
}


