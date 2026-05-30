package com.flashsalex.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlacklistRequest {

    @NotBlank(message = "目标类型不能为空")
    private String targetType; // USER / IP

    @NotBlank(message = "目标值不能为空")
    private String targetValue;

    private String reason;

    private LocalDateTime expireTime;
}
