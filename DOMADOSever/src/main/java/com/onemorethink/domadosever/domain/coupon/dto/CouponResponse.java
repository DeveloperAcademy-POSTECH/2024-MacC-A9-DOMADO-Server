package com.onemorethink.domadosever.domain.coupon.dto;

import com.onemorethink.domadosever.domain.coupon.entity.CouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "쿠폰 응답")
@Getter
@Builder
public class CouponResponse {
    @Schema(description = "쿠폰 ID", example = "123")
    private Long couponId;

    @Schema(description = "할인 시간(분)", example = "30")
    private Integer discountMinutes;

    @Schema(description = "쿠폰 상태")
    private CouponStatus status;

    @Schema(description = "만료 일시")
    private LocalDateTime expireDate;

    @Schema(description = "사용 일시")
    private LocalDateTime usedAt;

    @Schema(description = "사용된 결제 ID", example = "456")
    private Long usedPaymentId;
}
