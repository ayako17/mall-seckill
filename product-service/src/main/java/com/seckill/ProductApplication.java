package com.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 加上这行，告诉 MyBatis-Plus 去扫描这个包下的 Mapper 接口
@MapperScan("com.seckill.mapper") 
public class ProductApplication {
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(ProductApplication.class, args);
        System.out.println("====== 商品服务 (Product-Service) 启动成功！ ======");
    }
}