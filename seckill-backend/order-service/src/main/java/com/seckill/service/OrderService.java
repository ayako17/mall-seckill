package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.dto.OrderResponse;
import com.seckill.dto.SeckillOrderRequest;

public interface OrderService {
    
    /**
     * 秒杀下单
     */
    String createSeckillOrder(Long userId, SeckillOrderRequest request);
    
    /**
     * 查询订单结果
     */
    OrderResponse getOrderResult(String orderNo);
    
    /**
     * 查询用户订单列表
     */
    Page<OrderResponse> getUserOrders(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询订单详情
     */
    OrderResponse getOrderDetail(Long orderId);
}
