package com.onemorethink.domadosever.domain.payment.entity;

public enum PaymentStatus {
    PENDING("결제대기"),
    PROCESSING("처리중"),
    COMPLETED("결제완료"),
    FAILED("결제실패"),
    CANCELLED("취소됨"),
    REFUNDED("환불됨");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
