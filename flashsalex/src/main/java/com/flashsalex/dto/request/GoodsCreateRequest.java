package com.flashsalex.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoodsCreateRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 128, message = "商品名称最长128字")
    private String goodsName;

    private String goodsDesc;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal originalPrice;

    private String coverUrl;
}
