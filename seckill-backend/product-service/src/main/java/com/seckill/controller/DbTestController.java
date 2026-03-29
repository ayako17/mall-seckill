package com.seckill.controller;

import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test/db")
public class DbTestController {

    @Autowired
    private ProductService productService;

    @PostMapping("/write")
    @Transactional              // → 无 readOnly，路由到 MASTER
    public String write() {
        Product product = new Product();
        product.setId(System.currentTimeMillis());
        product.setName("Test-Product-" + System.currentTimeMillis());
        product.setPrice(new BigDecimal("99.99"));
        product.setDescription("Read-write split test");
        product.setStatus(1);

        boolean result = productService.save(product);
        log.info("Write operation result: {}, productId: {}", result, product.getId());
        return result ? "Write to master success, productId: " + product.getId() : "Write failed";
    }

    @GetMapping("/read")
    @Transactional(readOnly = true)   // → 路由到 SLAVE
    public List<Product> read() {
        List<Product> products = productService.list();
        log.info("Read operation, found {} products", products.size());
        return products;
    }
}
