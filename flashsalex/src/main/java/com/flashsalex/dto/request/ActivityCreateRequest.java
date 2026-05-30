package com.flashsalex.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityCreateRequest {

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 128, message = "活动名称最长128字")
    private String activityName;

    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价格必须大于0")
    private BigDecimal seckillPrice;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "库存不能为空")
    @Min(value = 1, message = "库存至少为1")
    private Integer totalStock;

    @Min(value = 1, message = "每人限购至少为1")
    private Integer perUserLimit = 1;
}
