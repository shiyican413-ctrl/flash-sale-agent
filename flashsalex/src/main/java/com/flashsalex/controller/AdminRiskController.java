package com.flashsalex.controller;

import com.flashsalex.common.Result;
import com.flashsalex.dto.request.BlacklistRequest;
import com.flashsalex.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-风控管理", description = "黑名单管理")
@RestController
@RequestMapping("/api/admin/risk")
@RequiredArgsConstructor
public class AdminRiskController {

    private final RiskService riskService;

    @Operation(summary = "添加黑名单")
    @PostMapping("/blacklist")
    public Result<Void> addBlacklist(@RequestBody @Valid BlacklistRequest request) {
        riskService.addToBlacklist(request.getTargetType(), request.getTargetValue(),
                request.getReason(), request.getExpireTime());
        return Result.success();
    }

    @Operation(summary = "移除黑名单")
    @DeleteMapping("/blacklist")
    public Result<Void> removeBlacklist(@RequestParam String targetType, @RequestParam String targetValue) {
        riskService.removeFromBlacklist(targetType, targetValue);
        return Result.success();
    }
}
