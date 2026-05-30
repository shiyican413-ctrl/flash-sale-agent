package com.flashsalex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillResultResponse {

    private String requestId;
    private String status; // PROCESSING / SUCCESS / FAILED
    private String orderNo;
}
