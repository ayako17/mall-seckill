package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.dto.OrderResponse;
import com.seckill.dto.SeckillOrderRequest;
import com.seckill.dto.payment.PaymentCallbackRequest;

public interface OrderService {

    String createSeckillOrder(Long userId, SeckillOrderRequest request);

    OrderResponse getOrderResult(String orderNo);

    Page<OrderResponse> getUserOrders(Long userId, Integer pageNum, Integer pageSize);

    OrderResponse getOrderDetail(Long orderId, Long userId);

    void handlePaymentCallback(PaymentCallbackRequest request);
}


