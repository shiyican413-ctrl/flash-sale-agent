package com.flashsalex.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoodsUpdateRequest {

    private String goodsName;
    private String goodsDesc;
    private BigDecimal originalPrice;
    private String coverUrl;
    private Integer status;
}
