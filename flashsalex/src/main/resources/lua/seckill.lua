-- 秒杀库存扣减 Lua 脚本
-- KEYS[1]: seckill:stock:{activityId}  库存key
-- KEYS[2]: seckill:user:set:{activityId}  已抢购用户集合key
-- ARGV[1]: userId 用户ID
-- 返回值: 0-成功  1-库存不足  2-重复购买

local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then
    return 1
end

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return 2
end

redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return 0
