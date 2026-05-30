package com.flashsalex.controller;

import com.flashsalex.common.Result;
import com.flashsalex.dto.request.SeckillRequest;
import com.flashsalex.dto.response.CaptchaResponse;
import com.flashsalex.dto.response.SeckillExecuteResponse;
import com.flashsalex.dto.response.SeckillPathResponse;
import com.flashsalex.dto.response.SeckillResultResponse;
import com.flashsalex.util.UserContext;
import com.flashsalex.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-秒杀抢购", description = "验证码、动态地址、秒杀请求、结果查询")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "获取验证码")
    @GetMapping("/activities/{activityId}/captcha")
    public Result<CaptchaResponse> getCaptcha(@PathVariable Long activityId) {
        Long userId = UserContext.requireCurrentUserId();
        String result = seckillService.getCaptcha(activityId, userId);
        String[] parts = result.split(":", 2);
        return Result.success(CaptchaResponse.builder()
                .captchaId(parts[0])
                .question(parts[1])
                .build());
    }

    @Operation(summary = "获取动态秒杀地址")
    @PostMapping("/activities/{activityId}/path")
    public Result<SeckillPathResponse> getSeckillPath(
            @PathVariable Long activityId,
            @RequestBody java.util.Map<String, String> body) {
        Long userId = UserContext.requireCurrentUserId();
        String path = seckillService.getSeckillPath(
                activityId, userId, body.get("captchaId"), body.get("captchaCode"));
        return Result.success(SeckillPathResponse.builder().path(path).build());
    }

    @Operation(summary = "发起秒杀")
    @PostMapping("/activities/{activityId}/purchase/{path}")
    public Result<SeckillExecuteResponse> executeSeckill(
            @PathVariable Long activityId,
            @PathVariable String path,
            @RequestBody(required = false) SeckillRequest request) {
        Long userId = UserContext.requireCurrentUserId();
        if (request == null) {
            request = new SeckillRequest();
        }
        String requestId = seckillService.executeSeckill(activityId, userId, path, request);
        return Result.success(SeckillExecuteResponse.builder()
                .requestId(requestId)
                .status("PROCESSING")
                .build());
    }

    @Operation(summary = "查询秒杀结果（通过活动ID）")
    @GetMapping("/activities/{activityId}/result")
    public Result<SeckillResultResponse> getSeckillResult(@PathVariable Long activityId) {
        Long userId = UserContext.requireCurrentUserId();
        return Result.success(seckillService.getSeckillResult(activityId, userId));
    }
}
