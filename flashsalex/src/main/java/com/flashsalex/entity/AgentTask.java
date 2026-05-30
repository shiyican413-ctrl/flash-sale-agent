package com.flashsalex.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_agent_task")
public class AgentTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    /**
     * ACTIVITY_PRECHECK / STOCK_RISK / ABNORMAL_ORDER / PRESSURE_REPORT
     */
    private String taskType;

    private String inputJson;

    private String outputText;

    /**
     * PROCESSING / SUCCESS / FAILED
     */
    private String status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
