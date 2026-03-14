package com.seckill.controller;

import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 之前的测试接口
    @GetMapping("/test")
    public String test() {
        return "商品服务访问成功！当前处理线程: " + Thread.currentThread().toString();
    }

    // 新增：获取所有秒杀商品列表 (符合你作业要求的 RESTful API)
    @GetMapping("/list")
    public List<Product> listProducts() {
        // productService.list() 会自动去阿里云数据库查询 product 表的所有数据
        return productService.list();
    }
}