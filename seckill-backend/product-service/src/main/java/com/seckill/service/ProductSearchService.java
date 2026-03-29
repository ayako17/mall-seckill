package com.seckill.service;

import com.seckill.document.ProductDocument;

import java.util.List;
import java.util.Map;

/**
 * 商品搜索服务接口
 */
public interface ProductSearchService {
    
    /**
     * 全文搜索商品（支持高亮）
     * @param keyword 搜索关键词
     * @return 搜索结果（包含高亮字段）
     */
    List<Map<String, Object>> searchWithHighlight(String keyword);
    
    /**
     * 简单搜索（不带高亮）
     */
    List<ProductDocument> simpleSearch(String keyword);
    
    /**
     * 同步单个商品到 ES
     */
    void syncProduct(Long productId);
    
    /**
     * 全量同步所有商品到 ES
     */
    void syncAllProducts();
    
    /**
     * 删除 ES 中的商品
     */
    void deleteProduct(Long productId);
}
