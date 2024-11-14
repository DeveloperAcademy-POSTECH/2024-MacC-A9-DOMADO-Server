package com.onemorethink.domadosever.domain.payment.entity.paymentMethod;

public enum PaymentMethodStatus {
    ACTIVE("사용가능"),
    INACTIVE("사용중지"),
    EXPIRED("만료됨");

    private final String description;

    PaymentMethodStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
