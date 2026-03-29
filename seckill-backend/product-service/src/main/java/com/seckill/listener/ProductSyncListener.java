// package com.seckill.listener;

// import com.seckill.document.ProductDocument;
// import com.seckill.entity.Product;
// import com.seckill.mapper.ProductMapper;
// import com.seckill.service.ProductSearchService;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.context.event.ApplicationReadyEvent;
// import org.springframework.context.event.EventListener;
// import org.springframework.stereotype.Component;

// import java.util.List;
// import java.util.stream.Collectors;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class ProductSyncListener {
    
//     private final ProductSearchService searchService;
//     private final ProductMapper productMapper;
    
//     @EventListener(ApplicationReadyEvent.class)
//     public void onApplicationReady() {
//         log.info("========== 开始同步商品数据到 ES ==========");
//         List<Product> products = productMapper.selectList(null);
//         List<ProductDocument> documents = products.stream()
//                 .map(this::convert)
//                 .collect(Collectors.toList());
//         searchService.batchSync(documents);
//         log.info("ES 同步完成，共 {} 条商品", documents.size());
//     }
    
//     private ProductDocument convert(Product product) {
//         ProductDocument doc = new ProductDocument();
//         doc.setId(product.getId());
//         doc.setName(product.getName());
//         doc.setDescription(product.getDescription());
//         doc.setMainImage(product.getMainImage());
//         doc.setPrice(product.getPrice());
//         doc.setStatus(product.getStatus());
//         return doc;
//     }
// }