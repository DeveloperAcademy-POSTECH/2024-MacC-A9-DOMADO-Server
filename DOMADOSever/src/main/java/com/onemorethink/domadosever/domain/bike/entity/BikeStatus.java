package com.onemorethink.domadosever.domain.bike.entity;

public enum BikeStatus {
    PARKED("주차"),
    IN_USE("사용중"),
    TEMPORARY_LOCKED("일시잠금"),
    MAINTENANCE("정비중"),
    LOW_BATTERY("배터리 부족"),
    OUT_OF_SERVICE("이용 불가");

    private final String description;

    BikeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
