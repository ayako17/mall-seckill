package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动商品表实体类
 * 对应数据库表：seckill_product
 * 独立的高并发状态机，承载特惠价格、独立库存与时间窗控制
 *
 * @author seckill
 * @since 2026-03-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("seckill_product")
public class SeckillProduct {

    /**
     * 秒杀活动主键ID (雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的基础商品ID (用于拉取商品详情图文)
     */
    private Long productId;

    /**
     * 秒杀列表专属主图URL (允许与普通主图不同，如加上“秒杀”角标)
     */
    private String mainImage;

    /**
     * 秒杀活动专属价格
     */
    private BigDecimal seckillPrice;

    /**
     * 分配给该场秒杀活动的总库存 (从普通库隔离而来)
     */
    private Integer seckillStock;

    /**
     * 秒杀剩余可用库存 (抢购扣减此项)
     */
    private Integer availableStock;

    /**
     * 单人限购数量 (防黄牛基础配置)
     */
    private Integer limitPerUser;

    /**
     * 秒杀开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 秒杀结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 活动状态: 0-未开始, 1-进行中, 2-已结束, 3-紧急取消下线
     */
    private Integer status;

    /**
     * 秒杀专用乐观锁版本号 (配合SQL防超卖)
     */
    @Version
    private Integer version;
}