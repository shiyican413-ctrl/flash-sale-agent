package com.flashsalex.controller;

import com.flashsalex.common.Result;
import com.flashsalex.util.UserContext;
import com.flashsalex.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-订单支付", description = "模拟支付、取消订单")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "模拟支付订单")
    @PostMapping("/{orderNo}/pay")
    public Result<Void> payOrder(@PathVariable String orderNo) {
        Long userId = UserContext.requireCurrentUserId();
        paymentService.payOrder(orderNo, userId);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        Long userId = UserContext.requireCurrentUserId();
        paymentService.cancelOrder(orderNo, userId);
        return Result.success();
    }
}
