package com.flashsalex.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀下单消息（发送到 MQ）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage implements Serializable {

    private String requestId;
    private Long userId;
    private Long activityId;
    private Long goodsId;
    private Integer quantity;
    private LocalDateTime createTime;
}
