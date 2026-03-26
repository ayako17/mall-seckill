package com.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.hash.BloomFilter;
import com.seckill.entity.Product;
import com.seckill.mapper.ProductMapper;
import com.seckill.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private BloomFilter<Long> bloomFilter;

    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:detail:";
    private static final long CACHE_TTL = 3600; // 基础过期时间 1小时
    private static final long NULL_CACHE_TTL = 300; // 空值缓存过期时间 5分钟
    private static final String LOCK_KEY_PREFIX = "lock:product:";
    
    private final Random random = new Random();

    @Override
    @Transactional(readOnly = true) 
    public Product getProductById(Long id) {
        log.info("========== 查询商品详情开始，id: {} ==========", id);
        
        // ========== 1. 布隆过滤器（防穿透）==========
        if (!bloomFilter.mightContain(id)) {
            log.info("布隆过滤器拦截非法ID: {}，商品不存在", id);
            return null;
        }
        
        String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
        String lockKey = LOCK_KEY_PREFIX + id;
        
        try {
            // ========== 2. 先查缓存 ==========
            String json = redisTemplate.opsForValue().get(cacheKey);
            
            if (json != null) {
                log.info("缓存命中，key: {}", cacheKey);
                if ("NULL".equals(json)) {
                    log.info("空值缓存，商品不存在");
                    return null;
                }
                return JSON.parseObject(json, Product.class);
            }
            
            // ========== 3. 缓存未命中，尝试获取分布式锁（防击穿）==========
            log.info("缓存未命中，尝试获取锁，key: {}", lockKey);
            String lockValue = String.valueOf(System.currentTimeMillis());
            Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 双重检查：获取锁后再次检查缓存
                    json = redisTemplate.opsForValue().get(cacheKey);
                    if (json != null) {
                        log.info("双重检查命中缓存");
                        if ("NULL".equals(json)) {
                            return null;
                        }
                        return JSON.parseObject(json, Product.class);
                    }
                    
                    // ========== 4. 查询数据库 ==========
                    log.info("获取锁成功，查询数据库，id: {}", id);
                    Product product = productMapper.selectById(id);
                    
                    // ========== 5. 回写缓存（防雪崩：随机过期时间）==========
                    if (product == null) {
                        log.info("商品不存在，缓存空值");
                        redisTemplate.opsForValue().set(cacheKey, "NULL", 
                            NULL_CACHE_TTL, TimeUnit.SECONDS);
                    } else {
                        // 随机过期时间：1小时 + 0-5分钟随机，防止雪崩
                        long expire = CACHE_TTL + random.nextInt(300);
                        log.info("商品存在，写入缓存，过期时间: {}秒", expire);
                        redisTemplate.opsForValue().set(cacheKey, 
                            JSON.toJSONString(product), expire, TimeUnit.SECONDS);
                    }
                    
                    return product;
                    
                } finally {
                    // 释放锁
                    redisTemplate.delete(lockKey);
                    log.info("释放锁，key: {}", lockKey);
                }
            } else {
                // 未获取到锁，等待后重试
                log.info("未获取到锁，等待重试...");
                Thread.sleep(50);
                return getProductById(id);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("等待锁被中断", e);
            return null;
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
    @Transactional
    public boolean updateProduct(Product product) {
        int rows = productMapper.updateById(product);
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
    @Transactional
    public boolean deleteProduct(Long id) {
        int rows = productMapper.deleteById(id);
        if (rows > 0) {
            String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
            redisTemplate.delete(cacheKey);
            log.info("商品删除，删除缓存，key: {}", cacheKey);
        }
        return rows > 0;
    }

    @Override
    @Transactional
    public void saveWithTransaction() {
        // 事务内写入
        Product product = new Product();
        product.setId(System.currentTimeMillis());
        product.setName("事务测试商品");
        product.setPrice(new BigDecimal("88.88"));
        product.setDescription("测试事务内读写分离");
        product.setStatus(1);
        this.save(product);
        
        // 事务内读取（应该在主库，因为事务上下文）
        List<Product> products = this.list();
        log.info("事务内查询到 {} 条记录", products.size());
        
        // 更新
        product.setName("事务测试商品-已更新");
        this.updateById(product);
        
        log.info("事务执行完成，所有操作应该在主库完成");
    }   
}