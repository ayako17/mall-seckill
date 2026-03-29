package com.seckill.config;

import com.google.common.hash.BloomFilter;
import com.seckill.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BloomFilterPreheater implements CommandLineRunner {
    
    @Autowired
    private BloomFilter<Long> bloomFilter;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public void run(String... args) {
        log.info("========== 开始预热布隆过滤器 ==========");
        
        // 加载所有商品 ID
        List<Long> productIds = productMapper.selectAllIds();
        for (Long id : productIds) {
            bloomFilter.put(id);
        }
        
        log.info("布隆过滤器预热完成，加载 {} 个商品ID", productIds.size());
        log.info("========== 布隆过滤器预热结束 ==========");
    }
}