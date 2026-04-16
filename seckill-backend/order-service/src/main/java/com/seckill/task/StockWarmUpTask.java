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
        log.info("寮€濮嬮鐑鏉€搴撳瓨鍒?Redis...");
        
        // 鏌ヨ鎵€鏈夎繘琛屼腑(status=1)浠ュ強鍗冲皢寮€濮嬩絾鏃堕棿宸插埌(status=0, start_time<=now)鐨勭鏉€娲诲姩
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
            log.info("棰勭儹搴撳瓨: seckillId={}, stock={}, status={}", product.getId(), product.getAvailableStock(), product.getStatus());
        }
        
        log.info("搴撳瓨棰勭儹瀹屾垚锛屽叡棰勭儹 {} 涓鏉€娲诲姩", seckillProducts.size());
    }
}


