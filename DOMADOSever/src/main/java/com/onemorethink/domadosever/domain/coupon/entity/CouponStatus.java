package com.onemorethink.domadosever.domain.coupon.entity;


import lombok.Getter;

@Getter
public enum CouponStatus {
    ACTIVE("사용 가능"),
    USED("사용 완료"),
    EXPIRED("기간 만료");

    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }
}
