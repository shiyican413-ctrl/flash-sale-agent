package com.flashsalex.common;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 统一错误码
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "success"),
    PARAM_ERROR(40001, "参数错误"),
    UNAUTHORIZED(40100, "未登录"),
    FORBIDDEN(40300, "无权限"),
    DUPLICATE_REQUEST(40901, "重复请求"),
    RATE_LIMITED(42900, "请求过于频繁"),
    SYSTEM_ERROR(50000, "系统异常"),

    // 业务错误码
    ACTIVITY_NOT_FOUND(60001, "活动不存在"),
    ACTIVITY_NOT_STARTED(60002, "活动未开始"),
    ACTIVITY_ENDED(60003, "活动已结束"),
    STOCK_NOT_ENOUGH(60004, "库存不足"),
    ALREADY_PURCHASED(60005, "已经抢购过"),
    CAPTCHA_ERROR(60006, "验证码错误"),
    ORDER_NOT_FOUND(60007, "订单不存在"),
    ORDER_STATUS_ERROR(60008, "订单状态异常"),
    USER_EXISTS(60009, "用户已存在"),
    USER_NOT_FOUND(60010, "用户不存在"),
    PASSWORD_ERROR(60011, "密码错误"),
    BLACKLISTED(60012, "已被加入黑名单");

    private final int code;
    private final String message;
}
