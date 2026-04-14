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

    @PostMapping("/seckill")
    public ApiResponse<String> createSeckillOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody SeckillOrderRequest request) {
        try {
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

    @GetMapping("/result/{orderNo}")
    public ApiResponse<OrderResponse> getOrderResult(@PathVariable String orderNo) {
        try {
            OrderResponse result = orderService.getOrderResult(orderNo);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

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

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = null;
            if (token != null && !token.isBlank()) {
                String actualToken = token.replace("Bearer ", "");
                userId = jwtUtil.getUserIdFromToken(actualToken);
            }
            OrderResponse order = orderService.getOrderDetail(orderId, userId);
            return ApiResponse.success(order);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
