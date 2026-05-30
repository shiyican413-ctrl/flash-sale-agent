package com.flashsalex.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_message_log")
public class MessageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String messageId;

    private String businessType;

    private String businessKey;

    /**
     * PROCESSING / SUCCESS / FAILED
     */
    private String status;

    private Integer retryCount;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
