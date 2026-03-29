package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {
    
    /**
     * 乐观锁扣减库存（带 version 校验，保留以便兼容）
     */
    @Update("UPDATE seckill_product SET available_stock = available_stock - #{quantity}, " +
            "version = version + 1 WHERE id = #{seckillId} AND available_stock >= #{quantity} " +
            "AND version = #{version}")
    int deductStock(@Param("seckillId") Long seckillId, 
                    @Param("quantity") Integer quantity, 
                    @Param("version") Integer version);

    /**
     * 直接扣减库存（不校验 version）
     * Redis 已作为第一道原子扣减门禁，此处只需同步 DB，无需乐观锁重试。
     */
    @Update("UPDATE seckill_product SET available_stock = available_stock - #{quantity}, " +
            "version = version + 1 WHERE id = #{seckillId} AND available_stock >= #{quantity}")
    int deductStockDirect(@Param("seckillId") Long seckillId,
                          @Param("quantity") Integer quantity);
}
