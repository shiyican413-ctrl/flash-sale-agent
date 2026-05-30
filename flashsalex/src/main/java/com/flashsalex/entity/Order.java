package com.flashsalex.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String requestId;

    private Long userId;

    private Long activityId;

    private Long goodsId;

    private Integer quantity;

    private BigDecimal payAmount;

    /**
     * WAIT_PAY / PAID / CANCELED / REFUNDED
     */
    private String status;

    private LocalDateTime expireTime;

    private LocalDateTime paidAt;

    private LocalDateTime canceledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
