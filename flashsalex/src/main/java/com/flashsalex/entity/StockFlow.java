package com.flashsalex.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_stock_flow")
public class StockFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String flowNo;

    private Long activityId;

    private String orderNo;

    private Long userId;

    /**
     * PRE_DEDUCT / ORDER_CREATED / PAY_SUCCESS / CANCEL_RESTORE / COMPENSATE
     */
    private String changeType;

    private Integer changeQuantity;

    private Integer beforeStock;

    private Integer afterStock;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
