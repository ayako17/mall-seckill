package com.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.dto.OrderResponse;
import com.seckill.dto.SeckillOrderRequest;
import com.seckill.entity.Order;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.OrderMapper;
import com.seckill.mapper.SeckillProductMapper;
import com.seckill.service.OrderService;
import com.seckill.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    private static final String SECKILL_ORDER_PENDING_KEY = "seckill:order:pending:";
    private static final String SECKILL_ORDER_SUCCESS_USERS_KEY = "seckill:order:success:";
    private static final String ORDER_RESULT_KEY = "seckill:order:result:";

    public OrderServiceImpl(OrderMapper orderMapper,
                            SeckillProductMapper seckillProductMapper,
                            StringRedisTemplate redisTemplate,
                            KafkaTemplate<String, String> kafkaTemplate,
                            SnowflakeIdGenerator snowflakeIdGenerator) {
        this.orderMapper = orderMapper;
        this.seckillProductMapper = seckillProductMapper;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }

    @Override
    public String createSeckillOrder(Long userId, SeckillOrderRequest request) {
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

        String successUsersKey = SECKILL_ORDER_SUCCESS_USERS_KEY + seckillId;
        Boolean alreadySuccess = redisTemplate.opsForSet().isMember(successUsersKey, String.valueOf(userId));
        if (Boolean.TRUE.equals(alreadySuccess)) {
            throw new RuntimeException("请勿重复下单");
        }

        String idempotentKey = SECKILL_ORDER_PENDING_KEY + userId + ":" + seckillId;
        Boolean setSuccess = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", 15, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(setSuccess)) {
            throw new RuntimeException("请勿重复下单");
        }

        Order order = new Order();
        order.setId(snowflakeIdGenerator.nextId());
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(seckillProduct.getProductId());
        order.setSeckillId(seckillId);
        order.setOrderType(1);
        order.setPayAmount(seckillProduct.getSeckillPrice().multiply(new java.math.BigDecimal(quantity)));
        order.setQuantity(quantity);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(15));

        try {
            kafkaTemplate.send("seckill-order", JSON.toJSONString(order));
            log.info("订单已发送到 Kafka: {}", order.getOrderNo());
        } catch (Exception e) {
            redisTemplate.delete(idempotentKey);
            log.error("Kafka 发送失败，已回滚幂等键: orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException("下单失败，请重试");
        }

        return order.getOrderNo();
    }

    @Override
    public OrderResponse getOrderResult(String orderNo) {
        String resultKey = ORDER_RESULT_KEY + orderNo;
        String result = redisTemplate.opsForValue().get(resultKey);

        if (result != null) {
            if ("SUCCESS".equals(result)) {
                Order order = orderMapper.selectOne(
                        new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
                );
                return convertToResponse(order);
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
        responsePage.setRecords(
                orderPage.getRecords().stream()
                        .map(this::convertToResponse)
                        .toList()
        );
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
            default -> response.setStatusDesc("未知状态");
        }

        return response;
    }
}
