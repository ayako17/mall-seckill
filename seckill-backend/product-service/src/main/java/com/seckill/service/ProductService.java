package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.entity.Product;

public interface ProductService extends IService<Product> {
    /**
     * 根据ID查询商品详情（带缓存）
     */
    Product getProductById(Long id);
}