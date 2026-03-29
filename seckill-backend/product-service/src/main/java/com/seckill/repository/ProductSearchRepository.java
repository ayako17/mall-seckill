// package com.seckill.repository;

// import com.seckill.document.ProductDocument;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
// import org.springframework.stereotype.Repository;

// import java.util.List;

// @Repository
// public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
//     // 方法1：返回 List（不带分页）
//     List<ProductDocument> findByNameContaining(String keyword);
    
//     List<ProductDocument> findByDescriptionContaining(String keyword);
    
//     List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
    
//     // 方法2：返回 Page（带分页）- 推荐
//     Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);
// }