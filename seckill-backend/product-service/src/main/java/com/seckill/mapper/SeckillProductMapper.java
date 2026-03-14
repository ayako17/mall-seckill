package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.SeckillProduct;
import com.seckill.vo.SeckillProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {

    /**
     * 自定义SQL：关联查询秒杀商品和基础商品信息
     * 过滤条件：活动未取消 (status != 3) 且 结束时间大于当前时间
     */
    @Select("SELECT sp.id AS seckill_id, sp.product_id, p.name AS product_name, " +
            "COALESCE(sp.main_image, p.main_image) AS main_image, " + // 如果秒杀主图为空，则用普通主图
            "p.price AS original_price, sp.seckill_price, sp.available_stock, " +
            "sp.start_time, sp.end_time, sp.status " +
            "FROM seckill_product sp " +
            "LEFT JOIN product p ON sp.product_id = p.id " +
            "WHERE sp.status != 3 AND sp.end_time >= NOW() " +
            "ORDER BY sp.start_time ASC")
    List<SeckillProductVO> getActiveSeckillProducts();
}