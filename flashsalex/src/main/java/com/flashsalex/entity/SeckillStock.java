package com.flashsalex.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_seckill_stock")
public class SeckillStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Integer totalStock;

    private Integer availableStock;

    private Integer lockedStock;

    private Integer soldStock;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
