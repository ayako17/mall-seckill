package com.seckill.task;

import com.seckill.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商品数据同步定时任务
 * 定期将 MySQL 数据同步到 ElasticSearch
 */
@Slf4j
@Component
public class ProductSyncTask {
    
    @Autowired
    private ProductSearchService productSearchService;
    
    /**
     * 每天凌晨 2 点执行全量同步
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncAllProducts() {
        log.info("========== 开始执行商品全量同步任务 ==========");
        try {
            productSearchService.syncAllProducts();
            log.info("========== 商品全量同步任务完成 ==========");
        } catch (Exception e) {
            log.error("商品全量同步任务失败", e);
        }
    }
}
