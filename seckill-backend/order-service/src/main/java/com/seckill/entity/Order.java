package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private Long productId;
    
    private Long seckillId;
    
    private Integer orderType;
    
    private BigDecimal payAmount;
    
    private Integer quantity;
    
    private Integer status;
    
    private Integer paymentMethod;
    
    private String transactionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}


