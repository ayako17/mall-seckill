package com.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.entity.Product;
import com.seckill.mapper.ProductMapper;
import com.seckill.service.ProductService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:detail:";
    private static final long CACHE_TTL = 3600; // 基础过期时间 1小时
    private static final long NULL_CACHE_TTL = 300; // 空值缓存过期时间 5分钟
    private static final String LOCK_KEY_PREFIX = "lock:product:";

    @Override
    public Product getProductById(Long id) {
        log.info("========== 查询商品详情开始，id: {} ==========", id);
        
        try {
            String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
            log.info("缓存key: {}", cacheKey);
            
            String json = redisTemplate.opsForValue().get(cacheKey);
            log.info("缓存结果: {}", json);

            // 1. 缓存命中
            if (json != null) {
                log.info("缓存命中，key: {}", cacheKey);
                if ("NULL".equals(json)) {
                    log.info("空值缓存，商品不存在");
                    return null;
                }
                try {
                    Product p = JSON.parseObject(json, Product.class);
                    log.info("缓存解析成功: {}", p);
                    return p;
                } catch (Exception e) {
                    log.error("缓存JSON解析失败", e);
                }
            }

            // 2. 缓存未命中，查询数据库
            log.info("缓存未命中，查询数据库，id: {}", id);
            Product product = productMapper.selectById(id);
            log.info("数据库查询结果: {}", product);

            // 3. 回填缓存
            if (product == null) {
                log.info("商品不存在，缓存空值");
                redisTemplate.opsForValue().set(cacheKey, "NULL", NULL_CACHE_TTL, TimeUnit.SECONDS);
            } else {
                log.info("商品存在，写入缓存");
                String productJson = JSON.toJSONString(product);
                redisTemplate.opsForValue().set(cacheKey, productJson, CACHE_TTL, TimeUnit.SECONDS);
            }

            return product;
            
        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            throw e;
        } finally {
            log.info("========== 查询商品详情结束 ==========");
        }
    }

    /**
     * 更新商品时删除缓存（保持数据一致性）
     */
    public boolean updateProduct(Product product) {
        // 1. 先更新数据库
        int rows = productMapper.updateById(product);
        // 2. 如果更新成功，删除缓存
        if (rows > 0 && product.getId() != null) {
            String cacheKey = PRODUCT_CACHE_KEY_PREFIX + product.getId();
            redisTemplate.delete(cacheKey);
            log.info("商品更新，删除缓存，key: {}", cacheKey);
        }
        return rows > 0;
    }

    /**
     * 删除商品时删除缓存
     */
    public boolean deleteProduct(Long id) {
        // 1. 先删除数据库
        int rows = productMapper.deleteById(id);
        // 2. 如果删除成功，删除缓存
        if (rows > 0) {
            String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
            redisTemplate.delete(cacheKey);
            log.info("商品删除，删除缓存，key: {}", cacheKey);
        }
        return rows > 0;
    }
}