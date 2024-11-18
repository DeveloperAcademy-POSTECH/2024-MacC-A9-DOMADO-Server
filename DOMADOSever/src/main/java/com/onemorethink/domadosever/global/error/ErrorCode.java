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

    // Bike Related Errors (B로 시작)
    BIKE_NOT_FOUND("B001", "자전거를 찾을 수 없습니다"),
    BIKE_NOT_AVAILABLE("B002", "현재 대여할 수 없는 자전거입니다"),
    LOW_BATTERY("B003", "배터리가 부족한 자전거입니다"),

    // Rental Related Errors (RT로 시작)
    ACTIVE_RENTAL_EXISTS("RT001", "이미 대여중인 자전거가 있습니다"),
    NO_PAYMENT_METHOD("RT002", "등록된 결제 수단이 없습니다"),
    RENTAL_NOT_FOUND("RT003", "해당 대여내역을 찾을 수 없습니다."),


    // Payment Related Errors (P로 시작)
    PAYMENT_METHOD_NOT_FOUND("P001", "결제 수단을 찾을 수 없습니다"),
    INVALID_CARD_NUMBER("P002", "유효하지 않은 카드 번호입니다"),
    INVALID_CARD_CVV("P003", "유효하지 않은 보안코드입니다"),
    EXPIRED_CARD("P004", "만료된 카드입니다"),
    PAYMENT_PROCESSING_FAILED("P005", "결제 처리 중 오류가 발생했습니다"),
    INVALID_PAYMENT_AMOUNT("P006", "유효하지 않은 결제 금액입니다"),
    INACTIVE_PAYMENT_METHOD("P007", "비활성화된 결제 수단입니다"),
    PAYMENT_METHOD_LIMIT_EXCEEDED("P008", "결제 수단 등록 한도를 초과했습니다"),
    DUPLICATE_PAYMENT("P009", "중복된 결제가 감지되었습니다"),
    PAYMENT_LIMIT_EXCEEDED("P010", "결제 한도를 초과했습니다"),
    INVALID_CARD_BIN("P011", "등록되지 않은 BIN 번호입니다"),
    DISCONTINUED_CARD_BIN("P012", "사용이 중지된 카드입니다"),
    INVALID_CARD_FORMAT("P013", "카드 번호 형식이 올바르지 않습니다"),
    INVALID_CARD_CHECKSUM("P014", "카드 번호 검증에 실패했습니다"),
    INVALID_CVV_FORMAT("P015", "보안코드(CVV) 형식이 올바르지 않습니다"),
    DAILY_PAYMENT_LIMIT_EXCEEDED("P016", "일일 결제 한도를 초과했습니다"),
    MONTHLY_PAYMENT_LIMIT_EXCEEDED("P017", "월간 결제 한도를 초과했습니다"),
    PAYMENT_TIMEOUT("P018", "결제 시간이 초과되었습니다"),
    BIN_LOAD_ERROR("P019", "BIN 정보 로딩 중 오류가 발생했습니다"),

    RENTAL_NOT_OWNED("RP001", "본인의 대여 내역만 조작할 수 있습니다"),
    RENTAL_NOT_IN_PROGRESS("RP002", "진행 중인 대여가 아닙니다"),
    BIKE_ALREADY_LOCKED("RP003", "이미 잠금 상태인 자전거입니다"),

    // Resume Related Errors (RR로 시작)
    BIKE_NOT_LOCKED("RR001", "잠금 상태가 아닌 자전거입니다"),
    BIKE_NOT_IN_PAUSE("RR002", "일시정지 상태가 아닙니다"),

    // MQTT Related Errors (M으로 시작)
    MQTT_PUBLISH_FAILED("M001", "자전거 잠금 해제 명령 전송에 실패했습니다"),

    // Role Related Errors (R로 시작)
    ROLE_NOT_FOUND("R001", "역할을 찾을 수 없습니다"),

    // Return Related Errors (RT로 시작)
    STATION_NOT_FOUND("RT004", "스테이션을 찾을 수 없습니다"),
    INVALID_RETURN_HUB("RT005", "올바르지 않은 반납 허브입니다"),
    RETURN_PROCESSING_FAILED("RT006","반납처리에 실패했습니다." ),

    // HiBike Related Errors (HB로 시작)
    ALREADY_HIBIKE("HB001", "이미 HiBike 상태입니다"),
    NOT_HIBIKE("HB002", "HiBike 상태가 아닙니다"),

    // Coupon Error (CO로 시작)
    INVALID_COUPON("CO001","유효하지 않은 쿠폰입니다"),
    NO_AVAILABLE_COUPON("CO002", "이용가능한 쿠폰이 없습니다" );

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}