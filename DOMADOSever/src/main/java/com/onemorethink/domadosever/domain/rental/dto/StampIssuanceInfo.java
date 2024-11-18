package com.onemorethink.domadosever.domain.rental.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StampIssuanceInfo {
    private boolean isIssued;
    private Long stampId;
    private int totalUnusedStamps;  // 현재 보유한 미사용 스탬프 수
    private boolean couponIssued;    // 스탬프로 인한 쿠폰 발급 여부
    private Long issuedCouponId;     // 발급된 쿠폰 ID
}