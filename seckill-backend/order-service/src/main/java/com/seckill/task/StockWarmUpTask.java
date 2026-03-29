package com.seckill.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.SeckillProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockWarmUpTask implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(StockWarmUpTask.class);
    
    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate redisTemplate;
    
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    
    public StockWarmUpTask(SeckillProductMapper seckillProductMapper,
                          StringRedisTemplate redisTemplate) {
        this.seckillProductMapper = seckillProductMapper;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public void run(String... args) {
        log.info("开始预热秒杀库存到 Redis...");
        
        // 查询所有进行中(status=1)以及即将开始但时间已到(status=0, start_time<=now)的秒杀活动
        LocalDateTime now = LocalDateTime.now();
        List<SeckillProduct> seckillProducts = seckillProductMapper.selectList(
            new LambdaQueryWrapper<SeckillProduct>()
                .and(w -> w
                    .eq(SeckillProduct::getStatus, 1)
                    .or()
                    .eq(SeckillProduct::getStatus, 0)
                    .le(SeckillProduct::getStartTime, now)
                )
        );
        
        for (SeckillProduct product : seckillProducts) {
            String stockKey = SECKILL_STOCK_KEY + product.getId();
            redisTemplate.opsForValue().set(stockKey, String.valueOf(product.getAvailableStock()));
            log.info("预热库存: seckillId={}, stock={}, status={}", product.getId(), product.getAvailableStock(), product.getStatus());
        }
        
        log.info("库存预热完成，共预热 {} 个秒杀活动", seckillProducts.size());
    }
}
