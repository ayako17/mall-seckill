package com.seckill.controller;

import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import com.seckill.service.SeckillProductService;
import com.seckill.vo.SeckillProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SeckillProductService seckillProductService;

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
}