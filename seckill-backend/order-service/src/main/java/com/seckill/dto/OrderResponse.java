package com.seckill.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private Long productId;
    
    private Long seckillId;
    
    private Integer orderType;
    
    private BigDecimal payAmount;
    
    private Integer quantity;
    
    private Integer status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    private String statusDesc;
}
