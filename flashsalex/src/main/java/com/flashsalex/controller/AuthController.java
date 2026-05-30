package com.flashsalex.controller;

import com.flashsalex.common.Result;
import com.flashsalex.dto.request.LoginRequest;
import com.flashsalex.dto.request.RegisterRequest;
import com.flashsalex.dto.response.LoginResponse;
import com.flashsalex.dto.response.UserInfo;
import com.flashsalex.util.UserContext;
import com.flashsalex.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户注册登录接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserInfo> getCurrentUser() {
        Long userId = UserContext.requireCurrentUserId();
        UserInfo userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}
