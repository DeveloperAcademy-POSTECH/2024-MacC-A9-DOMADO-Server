package com.onemorethink.domadosever.domain.user.entity;

public enum UserStatus {
    ACTIVE("정상"),
    SUSPENDED("이용정지"),
    BLOCKED("차단"),
    WITHDRAWN("탈퇴");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
