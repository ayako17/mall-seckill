-- KEYS[1]: seckill:stock:{seckillId}
-- KEYS[2]: seckill:order:pending:{seckillId}
-- KEYS[3]: seckill:order:success:{seckillId}
-- ARGV[1]: userId
-- ARGV[2]: quantity

local userId = ARGV[1]
local quantity = tonumber(ARGV[2])

if redis.call('SISMEMBER', KEYS[3], userId) == 1 then
    return -2
end

if redis.call('SISMEMBER', KEYS[2], userId) == 1 then
    return -2
end

local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
if stock < quantity then
    return -1
end

redis.call('DECRBY', KEYS[1], quantity)
redis.call('SADD', KEYS[2], userId)
redis.call('EXPIRE', KEYS[2], 1800)
return 1


