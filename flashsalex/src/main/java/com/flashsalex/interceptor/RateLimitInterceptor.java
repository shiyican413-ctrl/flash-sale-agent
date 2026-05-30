package com.flashsalex.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsalex.common.ErrorCode;
import com.flashsalex.common.RedisConstants;
import com.flashsalex.common.Result;
import com.flashsalex.service.RiskService;
import com.flashsalex.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 * 用户级限流 + IP级限流 + 黑名单校验
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final RiskService riskService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIp(request);

        // 1. IP 黑名单检查
        if (riskService.isIpBlacklisted(ip)) {
            log.warn("IP黑名单拦截: ip={}", ip);
            writeErrorResponse(response, ErrorCode.BLACKLISTED);
            return false;
        }

        // 2. IP 限流：每秒最多 10 次
        String ipRateKey = RedisConstants.RATE_IP_KEY + ip;
        if (!tryAcquire(ipRateKey, RedisConstants.RATE_IP_MAX, RedisConstants.RATE_IP_WINDOW)) {
            log.warn("IP限流: ip={}", ip);
            writeErrorResponse(response, ErrorCode.RATE_LIMITED);
            return false;
        }

        // 3. 用户级限流（已登录用户）
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) {
            // 用户黑名单检查
            if (riskService.isUserBlacklisted(userId)) {
                log.warn("用户黑名单拦截: userId={}", userId);
                writeErrorResponse(response, ErrorCode.BLACKLISTED);
                return false;
            }

            // 用户限流：5秒内最多 5 次
            String userRateKey = RedisConstants.RATE_USER_KEY + userId;
            if (!tryAcquire(userRateKey, RedisConstants.RATE_USER_MAX, RedisConstants.RATE_WINDOW)) {
                log.warn("用户限流: userId={}", userId);
                writeErrorResponse(response, ErrorCode.RATE_LIMITED);
                return false;
            }
        }

        return true;
    }

    /**
     * 基于 Redis 的滑动窗口限流（简化为计数器）
     */
    private boolean tryAcquire(String key, long maxCount, long windowSeconds) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count != null && count <= maxCount;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> result = Result.error(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
