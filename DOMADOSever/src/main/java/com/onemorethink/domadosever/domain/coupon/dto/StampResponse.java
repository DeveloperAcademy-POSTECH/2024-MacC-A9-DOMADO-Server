package com.onemorethink.domadosever.domain.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "스탬프 응답")
@Getter
@Builder
public class StampResponse {
    @Schema(description = "스탬프 ID", example = "123")
    private Long stampId;

    @Schema(description = "사용 여부", example = "false")
    private boolean isUsed;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "연관된 대여 ID", example = "456")
    private Long rentalId;

    @Schema(description = "교환된 쿠폰 ID", example = "789")
    private Long exchangedCouponId;
}

