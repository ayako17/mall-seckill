package com.seckill.repository;

import com.seckill.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品 ES Repository
 * 提供基础的 CRUD 和搜索功能
 */
@Repository
public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
    /**
     * 根据商品名称搜索（模糊匹配）
     */
    List<ProductDocument> findByNameContaining(String name);
    
    /**
     * 根据商品名称或描述搜索
     */
    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
}
