package com.flashsalex.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_risk_blacklist")
public class RiskBlacklist {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * USER / IP
     */
    private String targetType;

    private String targetValue;

    private String reason;

    private LocalDateTime expireTime;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
