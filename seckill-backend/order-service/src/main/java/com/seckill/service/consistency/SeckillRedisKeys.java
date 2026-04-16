package com.seckill.service.consistency;

public final class SeckillRedisKeys {

    private SeckillRedisKeys() {
    }

    public static final String STOCK = "seckill:stock:";
    public static final String PENDING_USERS = "seckill:order:pending:";
    public static final String SUCCESS_USERS = "seckill:order:success:";
    public static final String ORDER_RESULT = "seckill:order:result:";

    public static String stockKey(Long seckillId) {
        return STOCK + seckillId;
    }

    public static String pendingUsersKey(Long seckillId) {
        return PENDING_USERS + seckillId;
    }

    public static String successUsersKey(Long seckillId) {
        return SUCCESS_USERS + seckillId;
    }

    public static String orderResultKey(String orderNo) {
        return ORDER_RESULT + orderNo;
    }
}


