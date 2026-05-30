package com.flashsalex.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_seckill_activity")
public class SeckillActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;

    private String activityName;

    private BigDecimal seckillPrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 0-草稿 1-待开始 2-进行中 3-已结束 4-已下线
     */
    private Integer status;

    private Integer perUserLimit;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
