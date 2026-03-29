package com.seckill.service.impl;

import com.seckill.document.ProductDocument;
import com.seckill.entity.Product;
import com.seckill.mapper.ProductMapper;
import com.seckill.repository.ProductRepository;
import com.seckill.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<Map<String, Object>> searchWithHighlight(String keyword) {
        log.info("开始搜索商品，关键词: {}", keyword);
        
        // 构建查询条件：name 或 description 包含关键词
        Criteria criteria = new Criteria("name").contains(keyword)
                .or(new Criteria("description").contains(keyword));
        CriteriaQuery query = new CriteriaQuery(criteria);
        
        // 配置高亮
        HighlightField nameField = new HighlightField("name");
        HighlightField descField = new HighlightField("description");
        Highlight highlight = new Highlight(List.of(nameField, descField));
        
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);
        query.setHighlightQuery(highlightQuery);
        
        // 执行搜索
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
        
        // 处理结果
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit<ProductDocument> hit : searchHits) {
            Map<String, Object> result = new HashMap<>();
            ProductDocument doc = hit.getContent();
            
            result.put("id", doc.getId());
            result.put("price", doc.getPrice());
            result.put("stock", doc.getStock());
            result.put("status", doc.getStatus());
            
            // 处理高亮字段
            Map<String, List<String>> highlightFieldsMap = hit.getHighlightFields();
            if (highlightFieldsMap.containsKey("name")) {
                result.put("name", highlightFieldsMap.get("name").get(0));
            } else {
                result.put("name", doc.getName());
            }
            
            if (highlightFieldsMap.containsKey("description")) {
                result.put("description", highlightFieldsMap.get("description").get(0));
            } else {
                result.put("description", doc.getDescription());
            }
            
            results.add(result);
        }
        
        log.info("搜索完成，找到 {} 条结果", results.size());
        return results;
    }

    @Override
    public List<ProductDocument> simpleSearch(String keyword) {
        log.info("简单搜索，关键词: {}", keyword);
        return productRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    @Override
    public void syncProduct(Long productId) {
        log.info("同步商品到 ES，id: {}", productId);
        Product product = productMapper.selectById(productId);
        if (product != null) {
            ProductDocument doc = new ProductDocument();
            BeanUtils.copyProperties(product, doc);
            productRepository.save(doc);
            log.info("商品同步成功");
        } else {
            log.warn("商品不存在，id: {}", productId);
        }
    }

    @Override
    public void syncAllProducts() {
        log.info("开始全量同步商品到 ES");
        List<Product> products = productMapper.selectList(null);
        List<ProductDocument> documents = new ArrayList<>();
        
        for (Product product : products) {
            ProductDocument doc = new ProductDocument();
            BeanUtils.copyProperties(product, doc);
            documents.add(doc);
        }
        
        productRepository.saveAll(documents);
        log.info("全量同步完成，共同步 {} 条商品", documents.size());
    }

    @Override
    public void deleteProduct(Long productId) {
        log.info("删除 ES 中的商品，id: {}", productId);
        productRepository.deleteById(productId);
    }
}
