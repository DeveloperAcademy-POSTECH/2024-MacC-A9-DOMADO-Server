package com.onemorethink.domadosever.global.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common Errors (C로 시작)
    INVALID_INPUT_VALUE("C001", "잘못된 입력값입니다"),
    INTERNAL_SERVER_ERROR("C002", "내부 서버 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED("C003", "허용되지 않은 메소드입니다"),
    INVALID_TYPE_VALUE("C004", "잘못된 타입의 값입니다"),

    // Authentication & Authorization Errors (A로 시작)
    AUTHENTICATION_FAILED("A001", "인증에 실패했습니다"),
    TOKEN_REFRESH_FAILED("A002", "토큰 갱신에 실패했습니다"),
    LOGOUT_FAILED("A003", "로그아웃에 실패했습니다"),
    REGISTRATION_FAILED("A004", "회원가입에 실패했습니다"),
    INVALID_TOKEN("A005", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN("A006", "만료된 토큰입니다"),
    INVALID_REFRESH_TOKEN("A007", "유효하지 않은 리프레시 토큰입니다"),
    DUPLICATE_EMAIL("A008", "이미 사용 중인 이메일입니다"),
    INVALID_PASSWORD("A009", "잘못된 비밀번호입니다"),
    INVALID_PASSWORD_FORMAT("A010", "비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다"),
    INVALID_EMAIL_FORMAT("A011", "잘못된 이메일 형식입니다"),
    INVALID_PHONE_FORMAT("A012", "잘못된 전화번호 형식입니다"),
    SUSPICIOUS_TOKEN_REUSE("A013", "의심스러운 토큰 재사용이 감지되었습니다"),
    ACCESS_DENIED("A014", "접근이 거부되었습니다"),

    // User Related Errors (U로 시작)
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다"),
    USER_RETRIEVAL_FAILED("U002", "사용자 정보 조회에 실패했습니다"),
    ACCOUNT_LOCKED("U003", "계정이 잠겼습니다"),
    ACCOUNT_SUSPENDED("U004", "계정이 정지되었습니다"),
    ACCOUNT_BLOCKED("U005", "계정이 차단되었습니다"),
    ACCOUNT_WITHDRAWN("U006", "탈퇴한 계정입니다"),
    INVALID_USER_STATUS("U007", "잘못된 사용자 상태입니다"),

    // Location Related Errors (L로 시작)
    INVALID_COORDINATES("L001", "잘못된 좌표값입니다"),
    INVALID_RADIUS("L002", "잘못된 반경값입니다"),
    LOCATION_SERVICE_ERROR("L003", "위치 서비스 조회 중 오류가 발생했습니다"),

    // Role Related Errors (R로 시작)
    ROLE_NOT_FOUND("R001", "역할을 찾을 수 없습니다");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}