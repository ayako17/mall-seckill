package com.seckill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.config.dynamic.SeckillDynamicControlProperties;
import com.seckill.config.dynamic.SeckillTrafficGuard;
import com.seckill.dto.OrderResponse;
import com.seckill.dto.SeckillOrderRequest;
import com.seckill.dto.event.OrderPaidEvent;
import com.seckill.dto.event.SeckillOrderEvent;
import com.seckill.dto.payment.PaymentCallbackRequest;
import com.seckill.entity.Order;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.OrderMapper;
import com.seckill.mapper.SeckillProductMapper;
import com.seckill.service.OrderService;
import com.seckill.service.consistency.OrderOutboxService;
import com.seckill.service.consistency.RedisPreDeductService;
import com.seckill.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Service
@RefreshScope
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final RedisPreDeductService redisPreDeductService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final OrderOutboxService orderOutboxService;
    private final SeckillDynamicControlProperties dynamicControlProperties;
    private final SeckillTrafficGuard seckillTrafficGuard;

    @Value("${app.kafka.topic.seckill-order-create}")
    private String orderCreateTopic;

    public OrderServiceImpl(OrderMapper orderMapper,
                            SeckillProductMapper seckillProductMapper,
                            RedisPreDeductService redisPreDeductService,
                            KafkaTemplate<String, String> kafkaTemplate,
                            SnowflakeIdGenerator snowflakeIdGenerator,
                            OrderOutboxService orderOutboxService,
                            SeckillDynamicControlProperties dynamicControlProperties,
                            SeckillTrafficGuard seckillTrafficGuard) {
        this.orderMapper = orderMapper;
        this.seckillProductMapper = seckillProductMapper;
        this.redisPreDeductService = redisPreDeductService;
        this.kafkaTemplate = kafkaTemplate;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.orderOutboxService = orderOutboxService;
        this.dynamicControlProperties = dynamicControlProperties;
        this.seckillTrafficGuard = seckillTrafficGuard;
    }

    @Override
    @SentinelResource(
            value = "orderCreateSeckill",
            blockHandler = "createSeckillOrderBlock",
            fallback = "createSeckillOrderFallback"
    )
    public String createSeckillOrder(Long userId, SeckillOrderRequest request) {
        if (!dynamicControlProperties.isEnabled()) {
            throw new RuntimeException("秒杀通道已关闭");
        }
        if (!seckillTrafficGuard.tryAcquire(dynamicControlProperties.getRequestThresholdPerSecond())) {
            throw new RuntimeException("秒杀流量过高，请稍后重试");
        }

        Long seckillId = request.getSeckillId();
        Integer quantity = request.getQuantity() == null ? 1 : request.getQuantity();

        SeckillProduct seckillProduct = seckillProductMapper.selectById(seckillId);
        if (seckillProduct == null) {
            throw new RuntimeException("秒杀活动不存在");
        }
        if (seckillProduct.getStatus() != 1) {
            throw new RuntimeException("秒杀活动未开始或已结束");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillProduct.getStartTime()) || now.isAfter(seckillProduct.getEndTime())) {
            throw new RuntimeException("不在秒杀时间范围内");
        }

        long luaResult = redisPreDeductService.preDeduct(seckillId, userId, quantity);
        if (luaResult == -2) {
            throw new RuntimeException("请勿重复下单");
        }
        if (luaResult == -1) {
            throw new RuntimeException("库存不足");
        }
        if (luaResult != 1) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        Order order = new Order();
        order.setId(snowflakeIdGenerator.nextId());
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(seckillProduct.getProductId());
        order.setSeckillId(seckillId);
        order.setOrderType(1);
        order.setPayAmount(seckillProduct.getSeckillPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setQuantity(quantity);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(15));

        SeckillOrderEvent event = new SeckillOrderEvent();
        event.setTraceId(UUID.randomUUID().toString());
        event.setOrderId(order.getId());
        event.setOrderNo(order.getOrderNo());
        event.setUserId(order.getUserId());
        event.setProductId(order.getProductId());
        event.setSeckillId(order.getSeckillId());
        event.setQuantity(order.getQuantity());
        event.setPayAmount(order.getPayAmount());
        event.setCreateTime(order.getCreateTime());
        event.setExpireTime(order.getExpireTime());

        try {
            kafkaTemplate.send(orderCreateTopic, order.getOrderNo(), JSON.toJSONString(event));
            redisPreDeductService.setOrderResult(order.getOrderNo(), "PROCESSING", 10);
            log.info("order create event published, orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            redisPreDeductService.rollbackStock(seckillId, userId, quantity);
            throw new RuntimeException("下单失败，请重试");
        }

        return order.getOrderNo();
    }

    public String createSeckillOrderBlock(Long userId, SeckillOrderRequest request, BlockException ex) {
        throw new RuntimeException("下单请求已被限流/熔断，请稍后再试");
    }

    public String createSeckillOrderFallback(Long userId, SeckillOrderRequest request, Throwable ex) {
        throw new RuntimeException("下单服务繁忙，已触发降级");
    }

    @Override
    public OrderResponse getOrderResult(String orderNo) {
        String result = redisPreDeductService.getOrderResult(orderNo);

        if (result != null) {
            if ("SUCCESS".equals(result)) {
                Order order = orderMapper.selectByOrderNo(orderNo);
                return convertToResponse(order);
            }
            if ("PROCESSING".equals(result) || "RESERVING".equals(result)) {
                OrderResponse response = new OrderResponse();
                response.setOrderNo(orderNo);
                response.setStatusDesc("排队中，请稍后查询");
                return response;
            }
            throw new RuntimeException("下单失败: " + result);
        }

        OrderResponse response = new OrderResponse();
        response.setOrderNo(orderNo);
        response.setStatusDesc("排队中，请稍后查询");
        return response;
    }

    @Override
    public Page<OrderResponse> getUserOrders(Long userId, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> orderPage = orderMapper.selectPage(page,
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime)
        );

        Page<OrderResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(orderPage, responsePage, "records");
        responsePage.setRecords(orderPage.getRecords().stream().map(this::convertToResponse).toList());
        return responsePage;
    }

    @Override
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order;
        if (userId == null) {
            order = orderMapper.selectById(orderId);
        } else {
            order = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getId, orderId)
                            .eq(Order::getUserId, userId)
            );
        }

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return convertToResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentCallback(PaymentCallbackRequest request) {
        int rows = orderMapper.markOrderPaid(
                request.getOrderNo(),
                request.getPaymentMethod(),
                request.getTransactionId(),
                LocalDateTime.now()
        );

        if (rows == 0) {
            return;
        }

        Order order = orderMapper.selectByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new RuntimeException("支付回调对应订单不存在");
        }

        OrderPaidEvent event = new OrderPaidEvent();
        event.setOrderNo(order.getOrderNo());
        event.setUserId(order.getUserId());
        event.setProductId(order.getProductId());
        event.setSeckillId(order.getSeckillId());
        event.setQuantity(order.getQuantity());
        event.setTransactionId(request.getTransactionId());
        event.setPaymentMethod(request.getPaymentMethod());
        event.setPaidTime(LocalDateTime.now());

        orderOutboxService.createOrderPaidOutbox(order, JSON.toJSONString(event));
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "MS" + timestamp + random;
    }

    private OrderResponse convertToResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();
        BeanUtils.copyProperties(order, response);

        switch (order.getStatus()) {
            case 0 -> response.setStatusDesc("待支付");
            case 1 -> response.setStatusDesc("已支付");
            case 2 -> response.setStatusDesc("超时已取消");
            case 3 -> response.setStatusDesc("用户主动取消");
            case 4 -> response.setStatusDesc("已退款");
            case 6 -> response.setStatusDesc("库存预留失败已取消");
            default -> response.setStatusDesc("未知状态");
        }

        return response;
    }
}