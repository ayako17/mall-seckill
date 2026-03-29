package com.seckill.controller;
import com.seckill.document.ProductDocument;
import com.seckill.entity.Product;
import com.seckill.service.ProductSearchService;
import com.seckill.service.ProductService;
import com.seckill.service.SeckillProductService;
import com.seckill.vo.SeckillProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SeckillProductService seckillProductService;
    
    @Autowired
    private ProductSearchService productSearchService;


    // 1. 获取所有普通商品列表
    @GetMapping("/normal/list")
    public List<Product> listNormalProducts() {
        return productService.list();
    }

    // 2. 获取秒杀商品列表
    @GetMapping("/seckill/list")
    public List<SeckillProductVO> listSeckillProducts() {
        return seckillProductService.getActiveSeckillProducts();
    }

    // 3. 【新增】获取单个商品详情（带缓存）
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
    
    // ========== ElasticSearch 搜索接口 ==========
    
    /**
     * 全文搜索（带高亮）
     * GET /api/products/search?keyword=手机
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam String keyword) {
        List<Map<String, Object>> results = productSearchService.searchWithHighlight(keyword);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 简单搜索（不带高亮）
     */
    @GetMapping("/search/simple")
    public ResponseEntity<List<ProductDocument>> simpleSearch(@RequestParam String keyword) {
        List<ProductDocument> results = productSearchService.simpleSearch(keyword);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 手动同步单个商品到 ES
     */
    @PostMapping("/sync/{id}")
    public ResponseEntity<String> syncProduct(@PathVariable Long id) {
        productSearchService.syncProduct(id);
        return ResponseEntity.ok("商品同步成功");
    }
    
    /**
     * 手动全量同步所有商品到 ES
     */
    @PostMapping("/sync/all")
    public ResponseEntity<String> syncAllProducts() {
        productSearchService.syncAllProducts();
        return ResponseEntity.ok("全量同步成功");
    }
}
