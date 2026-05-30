package com.flashsalex.common;

/**
 * Redis Key 常量
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    /** 活动详情 seckill:activity:{activityId} */
    public static final String ACTIVITY_KEY = "seckill:activity:";

    /** 活动库存 seckill:stock:{activityId} */
    public static final String STOCK_KEY = "seckill:stock:";

    /** 已抢购用户集合 seckill:user:set:{activityId} */
    public static final String USER_SET_KEY = "seckill:user:set:";

    /** 秒杀结果 seckill:result:{activityId}:{userId} */
    public static final String RESULT_KEY = "seckill:result:";

    /** 用户限流 seckill:rate:user:{userId} */
    public static final String RATE_USER_KEY = "seckill:rate:user:";

    /** IP 限流 seckill:rate:ip:{ip} */
    public static final String RATE_IP_KEY = "seckill:rate:ip:";

    /** 动态秒杀地址 seckill:path:{activityId}:{userId} */
    public static final String PATH_KEY = "seckill:path:";

    /** 验证码 seckill:captcha:{activityId}:{userId} */
    public static final String CAPTCHA_KEY = "seckill:captcha:";

    /** 活动缓存过期时间（秒） */
    public static final long ACTIVITY_EXPIRE = 3600;

    /** 动态地址过期时间（秒） */
    public static final long PATH_EXPIRE = 60;

    /** 验证码过期时间（秒） */
    public static final long CAPTCHA_EXPIRE = 300;

    /** 秒杀结果过期时间（秒） */
    public static final long RESULT_EXPIRE = 3600;

    /** 用户限流窗口时间（秒） */
    public static final long RATE_WINDOW = 5;

    /** 用户限流最大次数 */
    public static final long RATE_USER_MAX = 5;

    /** IP限流窗口时间（秒） */
    public static final long RATE_IP_WINDOW = 1;

    /** IP限流最大次数 */
    public static final long RATE_IP_MAX = 10;
}
