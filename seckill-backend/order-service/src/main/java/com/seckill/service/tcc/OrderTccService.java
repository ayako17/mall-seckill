package com.seckill.service.tcc;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;
import com.seckill.entity.Order;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.consistency.RedisPreDeductService;
import com.seckill.util.SnowflakeIdGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class OrderTccService {

    private static final String ORDER_TCC_TRY_KEY = "tcc:order:try:";

    private final StringRedisTemplate redisTemplate;
    private final OrderMapper orderMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final RedisPreDeductService redisPreDeductService;

    public OrderTccService(StringRedisTemplate redisTemplate,
                           OrderMapper orderMapper,
                           SnowflakeIdGenerator snowflakeIdGenerator,
                           RedisPreDeductService redisPreDeductService) {
        this.redisTemplate = redisTemplate;
        this.orderMapper = orderMapper;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.redisPreDeductService = redisPreDeductService;
    }

    public TccResponse tryReserve(TccOrderRequest request) {
        String key = ORDER_TCC_TRY_KEY + request.getXid();
        if (request.getOrderNo() == null || request.getOrderNo().isBlank()) {
            request.setOrderNo("TCC" + snowflakeIdGenerator.nextId());
        }
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, JSON.toJSONString(request), 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(ok)) {
            return TccResponse.fail("order try duplicated");
        }
        return TccResponse.ok("order try success");
    }

    public TccResponse confirm(String xid) {
        String key = ORDER_TCC_TRY_KEY + xid;
        String payload = redisTemplate.opsForValue().get(key);
        if (payload == null) {
            return TccResponse.ok("order confirm idempotent");
        }

        TccOrderRequest req = JSON.parseObject(payload, TccOrderRequest.class);
        Order existed = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, req.getOrderNo()));
        if (existed == null) {
            Order order = new Order();
            order.setId(snowflakeIdGenerator.nextId());
            order.setOrderNo(req.getOrderNo());
            order.setUserId(req.getUserId());
            order.setProductId(req.getProductId());
            order.setSeckillId(req.getSeckillId());
            order.setOrderType(1);
            order.setQuantity(req.getQuantity());
            order.setPayAmount(req.getPayAmount());
            order.setStatus(0);
            order.setCreateTime(LocalDateTime.now());
            order.setExpireTime(LocalDateTime.now().plusMinutes(15));
            orderMapper.insert(order);
        }

        redisPreDeductService.markOrderSuccess(req.getSeckillId(), req.getUserId());
        redisTemplate.delete(key);
        return TccResponse.ok("order confirm success");
    }

    public TccResponse cancel(String xid) {
        String key = ORDER_TCC_TRY_KEY + xid;
        String payload = redisTemplate.opsForValue().get(key);
        if (payload != null) {
            TccOrderRequest req = JSON.parseObject(payload, TccOrderRequest.class);
            redisPreDeductService.rollbackStock(req.getSeckillId(), req.getUserId(), req.getQuantity());
            redisTemplate.delete(key);
        }
        return TccResponse.ok("order cancel success");
    }
}


