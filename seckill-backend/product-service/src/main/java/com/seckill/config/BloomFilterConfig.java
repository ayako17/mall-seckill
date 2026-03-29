package com.seckill.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {
    
    @Bean
    public BloomFilter<Long> productBloomFilter() {
        // 预计插入 10 万条商品，误判率 1%
        return BloomFilter.create(Funnels.longFunnel(), 100000, 0.01);
    }
}