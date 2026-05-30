package com.flashsalex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetailResponse {

    private Long activityId;
    private String activityName;
    private Long goodsId;
    private String goodsName;
    private BigDecimal seckillPrice;
    private BigDecimal originalPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer totalStock;
    private Integer availableStock;
    private Integer perUserLimit;
}
