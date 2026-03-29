package com.seckill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.dto.OrderResponse;
import com.seckill.dto.SeckillOrderRequest;
import com.seckill.service.OrderService;
import com.seckill.util.JwtUtil;
import com.seckill.vo.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    private final OrderService orderService;
    private final JwtUtil jwtUtil;
    
    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * 秒杀下单
     */
    @PostMapping("/seckill")
    public ApiResponse<String> createSeckillOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody SeckillOrderRequest request) {
        try {
            // 解析 token 获取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return ApiResponse.error(401, "未登录或登录已过期");
            }
            
            String orderNo = orderService.createSeckillOrder(userId, request);
            return ApiResponse.success("下单成功，排队中", orderNo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 查询订单结果
     */
    @GetMapping("/result/{orderNo}")
    public ApiResponse<OrderResponse> getOrderResult(@PathVariable String orderNo) {
        try {
            OrderResponse result = orderService.getOrderResult(orderNo);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 查询用户订单列表
     */
    @GetMapping("/list")
    public ApiResponse<Page<OrderResponse>> getUserOrders(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return ApiResponse.error(401, "未登录或登录已过期");
            }
            
            Page<OrderResponse> page = orderService.getUserOrders(userId, pageNum, pageSize);
            return ApiResponse.success(page);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderDetail(@PathVariable Long orderId) {
        try {
            OrderResponse order = orderService.getOrderDetail(orderId);
            return ApiResponse.success(order);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
