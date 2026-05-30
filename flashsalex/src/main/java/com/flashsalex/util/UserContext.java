package com.flashsalex.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户上下文工具类 - 从 SecurityContext 中获取当前登录用户信息
 */
public final class UserContext {

    private UserContext() {
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID，未登录抛异常
     */
    public static Long requireCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new com.flashsalex.common.BusinessException(com.flashsalex.common.ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
