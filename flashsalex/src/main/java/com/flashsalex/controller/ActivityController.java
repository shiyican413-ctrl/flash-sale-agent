package com.flashsalex.controller;

import com.flashsalex.common.Result;
import com.flashsalex.dto.response.ActivityDetailResponse;
import com.flashsalex.service.SeckillActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端-秒杀活动", description = "活动列表和详情")
@RestController
@RequestMapping("/api/seckill/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final SeckillActivityService activityService;

    @Operation(summary = "活动详情")
    @GetMapping("/{activityId}")
    public Result<ActivityDetailResponse> getActivityDetail(@PathVariable Long activityId) {
        return Result.success(activityService.getActivityDetail(activityId));
    }
}
