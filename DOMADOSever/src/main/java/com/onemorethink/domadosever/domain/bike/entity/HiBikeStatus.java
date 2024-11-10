package com.onemorethink.domadosever.domain.bike.entity;

public enum HiBikeStatus {
    NONE("HiBike 아님"),
    AVAILABLE_FOR_RENT("HiBike 대여 가능"),
    TRANSFERRED("HiBike 이관 완료");

    private final String description;

    HiBikeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
