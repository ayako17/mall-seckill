package com.seckill.controller;

import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import com.seckill.service.SeckillProductService;
import com.seckill.vo.SeckillProductVO;
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

    @Autowired
    private SeckillProductService seckillProductService;

    // 1. 获取所有【普通商品】列表
    @GetMapping("/normal/list")
    public List<Product> listNormalProducts() {
        // 这里后续可以加上条件：只查 status=1 (已上架) 的商品
        return productService.list();
    }

    // 2. 获取【今日必抢/限时秒杀】商品列表 (前端对应的就是这个接口)
    @GetMapping("/seckill/list")
    public List<SeckillProductVO> listSeckillProducts() {
        // 返回包含 商品名、原价、秒杀价等整合好的视图数据
        return seckillProductService.getActiveSeckillProducts();
    }
}