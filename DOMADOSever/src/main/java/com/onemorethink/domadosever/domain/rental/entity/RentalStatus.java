package com.onemorethink.domadosever.domain.rental.entity;

public enum RentalStatus {
    IN_PROGRESS("대여중"),
    PAUSED("일시정지"),
    COMPLETED("반납완료"),
    OVERDUE("연체"),
    FORCIBLY_ENDED("강제종료");

    private final String description;

    RentalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
