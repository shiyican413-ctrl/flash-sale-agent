package com.flashsalex.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flashsalex.common.Result;
import com.flashsalex.dto.request.ActivityCreateRequest;
import com.flashsalex.entity.SeckillActivity;
import com.flashsalex.service.CachePreheatService;
import com.flashsalex.service.SeckillActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-秒杀活动管理", description = "活动创建、上下线、预热")
@RestController
@RequestMapping("/api/admin/seckill/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final SeckillActivityService activityService;
    private final CachePreheatService cachePreheatService;

    @Operation(summary = "创建秒杀活动")
    @PostMapping
    public Result<Long> createActivity(@RequestBody @Valid ActivityCreateRequest request) {
        Long id = activityService.createActivity(request);
        return Result.success(id);
    }

    @Operation(summary = "活动上线")
    @PostMapping("/{activityId}/online")
    public Result<Void> onlineActivity(@PathVariable Long activityId) {
        activityService.onlineActivity(activityId);
        return Result.success();
    }

    @Operation(summary = "活动下线")
    @PostMapping("/{activityId}/offline")
    public Result<Void> offlineActivity(@PathVariable Long activityId) {
        activityService.offlineActivity(activityId);
        return Result.success();
    }

    @Operation(summary = "缓存预热")
    @PostMapping("/{activityId}/preheat")
    public Result<Void> preheatActivity(@PathVariable Long activityId) {
        cachePreheatService.preheatActivity(activityId);
        return Result.success();
    }

    @Operation(summary = "缓存一致性校验")
    @GetMapping("/{activityId}/cache-verify")
    public Result<Boolean> verifyCache(@PathVariable Long activityId) {
        return Result.success(cachePreheatService.verifyConsistency(activityId));
    }

    @Operation(summary = "活动列表")
    @GetMapping
    public Result<Page<SeckillActivity>> listActivities(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(activityService.listActivities(status, page, size));
    }
}
