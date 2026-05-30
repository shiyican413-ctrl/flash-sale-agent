package com.flashsalex.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashsalex.common.Result;
import com.flashsalex.entity.Order;
import com.flashsalex.util.UserContext;
import com.flashsalex.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-订单管理", description = "订单列表、详情")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<Page<Order>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.requireCurrentUserId();
        return Result.success(orderService.listOrders(userId, page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderByOrderNo(orderNo));
    }
}
