package com.seckill.config.dynamic;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SeckillTrafficGuard {

    private final AtomicLong currentSecond = new AtomicLong(0);
    private final AtomicInteger currentCount = new AtomicInteger(0);

    public boolean tryAcquire(int threshold) {
        long second = System.currentTimeMillis() / 1000;
        long old = currentSecond.get();
        if (old != second && currentSecond.compareAndSet(old, second)) {
            currentCount.set(0);
        }
        return currentCount.incrementAndGet() <= threshold;
    }
}