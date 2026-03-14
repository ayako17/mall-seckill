package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.entity.SeckillProduct;
import com.seckill.vo.SeckillProductVO;
import java.util.List;

public interface SeckillProductService extends IService<SeckillProduct> {
    // 获取有效秒杀商品列表
    List<SeckillProductVO> getActiveSeckillProducts();
}