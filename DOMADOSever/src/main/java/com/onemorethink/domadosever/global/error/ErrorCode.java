package com.onemorethink.domadosever.global.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common Errors
    INVALID_INPUT_VALUE("C001", "Invalid input value"),
    INTERNAL_SERVER_ERROR("C002", "Internal server error"),

    // User Domain Errors
    USER_NOT_FOUND("U001", "User not found"),
    DUPLICATE_EMAIL("U002", "Email already exists"),

    // Bike Domain Errors
    BIKE_NOT_FOUND("B001", "Bike not found"),
    BIKE_ALREADY_IN_USE("B002", "Bike is already in use"),

    // Rental Domain Errors
    RENTAL_NOT_FOUND("R001", "Rental not found"),
    INVALID_RENTAL_STATUS("R002", "Invalid rental status"),

    // Payment Domain Errors
    PAYMENT_FAILED("P001", "Payment processing failed"),
    INSUFFICIENT_BALANCE("P002", "Insufficient balance");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}