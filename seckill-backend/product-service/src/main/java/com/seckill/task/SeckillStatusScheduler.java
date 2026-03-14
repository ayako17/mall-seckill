package com.seckill.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.SeckillProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 秒杀状态定时任务
 * 自动更新秒杀活动的状态
 */
@Slf4j
@Component
@EnableScheduling
public class SeckillStatusScheduler {

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    /**
     * 每30秒执行一次，更新秒杀活动状态
     */
    @Scheduled(cron = "0/30 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillStatus() {
        log.info("========== 开始执行秒杀状态更新任务 ==========");
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 将进行中的活动更新为"进行中"状态
        LambdaUpdateWrapper<SeckillProduct> activeWrapper = new LambdaUpdateWrapper<>();
        activeWrapper
            .eq(SeckillProduct::getStatus, 0)  // 当前状态为"未开始"
            .le(SeckillProduct::getStartTime, now)  // 开始时间 <= 当前时间
            .ge(SeckillProduct::getEndTime, now)    // 结束时间 >= 当前时间
            .set(SeckillProduct::getStatus, 1);     // 更新为"进行中"
        
        int activeCount = seckillProductMapper.update(null, activeWrapper);
        if (activeCount > 0) {
            log.info("已将 {} 个秒杀活动更新为【进行中】状态", activeCount);
        }
        
        // 2. 将已结束的活动更新为"已结束"状态
        LambdaUpdateWrapper<SeckillProduct> expiredWrapper = new LambdaUpdateWrapper<>();
        expiredWrapper
            .in(SeckillProduct::getStatus, 0, 1)  // 未开始或进行中
            .lt(SeckillProduct::getEndTime, now)   // 结束时间 < 当前时间
            .set(SeckillProduct::getStatus, 2);    // 更新为"已结束"
        
        int expiredCount = seckillProductMapper.update(null, expiredWrapper);
        if (expiredCount > 0) {
            log.info("已将 {} 个秒杀活动更新为【已结束】状态", expiredCount);
        }
        
        log.info("========== 秒杀状态更新任务执行完成 ==========");
    }

    /**
     * 每天凌晨2点，清理过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        log.info("开始清理过期秒杀数据...");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        LambdaUpdateWrapper<SeckillProduct> cleanWrapper = new LambdaUpdateWrapper<>();
        cleanWrapper
            .eq(SeckillProduct::getStatus, 2)  // 已结束
            .lt(SeckillProduct::getEndTime, thirtyDaysAgo)  // 结束超过30天
            .set(SeckillProduct::getStatus, 4);  // 标记为"已归档"
        
        int cleanCount = seckillProductMapper.update(null, cleanWrapper);
        log.info("已清理 {} 条过期秒杀数据", cleanCount);
    }
}