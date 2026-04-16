package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {
    
    /**
     * 涔愯閿佹墸鍑忓簱瀛橈紙甯?version 鏍￠獙锛屼繚鐣欎互渚垮吋瀹癸級
     */
    @Update("UPDATE seckill_product SET available_stock = available_stock - #{quantity}, " +
            "version = version + 1 WHERE id = #{seckillId} AND available_stock >= #{quantity} " +
            "AND version = #{version}")
    int deductStock(@Param("seckillId") Long seckillId, 
                    @Param("quantity") Integer quantity, 
                    @Param("version") Integer version);

    /**
     * 鐩存帴鎵ｅ噺搴撳瓨锛堜笉鏍￠獙 version锛?
     * Redis 宸蹭綔涓虹涓€閬撳師瀛愭墸鍑忛棬绂侊紝姝ゅ鍙渶鍚屾 DB锛屾棤闇€涔愯閿侀噸璇曘€?
     */
    @Update("UPDATE seckill_product SET available_stock = available_stock - #{quantity}, " +
            "version = version + 1 WHERE id = #{seckillId} AND available_stock >= #{quantity}")
    int deductStockDirect(@Param("seckillId") Long seckillId,
                          @Param("quantity") Integer quantity);
}


