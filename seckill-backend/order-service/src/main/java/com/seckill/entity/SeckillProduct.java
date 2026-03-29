package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_product")
public class SeckillProduct {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long productId;
    
    private String mainImage;
    
    private BigDecimal seckillPrice;
    
    private Integer seckillStock;
    
    private Integer availableStock;
    
    private Integer limitPerUser;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    private Integer status;
    
    private Integer version;
}
