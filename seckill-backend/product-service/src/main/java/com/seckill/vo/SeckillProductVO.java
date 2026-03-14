package com.seckill.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 专门用于返回给前端展示的秒杀商品视图对象
 */
public class SeckillProductVO {
    private Long seckillId;       // 秒杀活动ID (用于下单)
    private Long productId;       // 基础商品ID (用于跳转详情页)
    private String productName;   // 商品名称 (来自 product 表)
    private String mainImage;     // 商品主图 (优先取秒杀表的主图)
    private BigDecimal originalPrice; // 原价 (来自 product 表)
    private BigDecimal seckillPrice;  // 秒杀价
    private Integer availableStock;   // 剩余可用库存
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;  // 开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;    // 结束时间
    private Integer status;           // 状态

    // Getter 和 Setter
    public Long getSeckillId() { return seckillId; }
    public void setSeckillId(Long seckillId) { this.seckillId = seckillId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getSeckillPrice() { return seckillPrice; }
    public void setSeckillPrice(BigDecimal seckillPrice) { this.seckillPrice = seckillPrice; }

    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}