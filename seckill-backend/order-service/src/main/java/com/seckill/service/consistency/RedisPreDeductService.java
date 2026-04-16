package com.seckill.service.consistency;

import jakarta.annotation.PostConstruct;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
public class RedisPreDeductService {

    private final RedissonClient redissonClient;

    private String preDeductLua;

    public RedisPreDeductService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @PostConstruct
    public void init() throws IOException {
        try (var in = new ClassPathResource("lua/seckill_pre_deduct.lua").getInputStream()) {
            this.preDeductLua = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public long preDeduct(Long seckillId, Long userId, int quantity) {
        Number result = redissonClient.getScript(StringCodec.INSTANCE)
                .eval(
                        RScript.Mode.READ_WRITE,
                        preDeductLua,
                        RScript.ReturnType.INTEGER,
                        List.of(
                                SeckillRedisKeys.stockKey(seckillId),
                                SeckillRedisKeys.pendingUsersKey(seckillId),
                                SeckillRedisKeys.successUsersKey(seckillId)
                        ),
                        String.valueOf(userId),
                        String.valueOf(quantity)
                );
        return result == null ? -99L : result.longValue();
    }

    public void markOrderSuccess(Long seckillId, Long userId) {
        String pendingKey = SeckillRedisKeys.pendingUsersKey(seckillId);
        String successKey = SeckillRedisKeys.successUsersKey(seckillId);

        RSet<String> pendingSet = redissonClient.getSet(pendingKey, StringCodec.INSTANCE);
        RSet<String> successSet = redissonClient.getSet(successKey, StringCodec.INSTANCE);
        pendingSet.remove(String.valueOf(userId));
        successSet.add(String.valueOf(userId));
        successSet.expire(Duration.ofDays(7));
    }

    public void rollbackStock(Long seckillId, Long userId, int quantity) {
        RAtomicLong stock = redissonClient.getAtomicLong(SeckillRedisKeys.stockKey(seckillId));
        stock.addAndGet(quantity);

        RSet<String> pendingSet = redissonClient.getSet(SeckillRedisKeys.pendingUsersKey(seckillId), StringCodec.INSTANCE);
        pendingSet.remove(String.valueOf(userId));
    }

    public void setOrderResult(String orderNo, String result, long minutes) {
        RBucket<String> bucket = redissonClient.getBucket(SeckillRedisKeys.orderResultKey(orderNo), StringCodec.INSTANCE);
        bucket.set(result, Duration.ofMinutes(minutes));
    }

    public String getOrderResult(String orderNo) {
        return (String) redissonClient.getBucket(SeckillRedisKeys.orderResultKey(orderNo), StringCodec.INSTANCE).get();
    }
}


