package com.onemorethink.domadosever.domain.payment.entity.paymentMethod;

public enum CardType {
    CREDIT("신용"),
    DEBIT("체크"),
    GIFT("기프트");

    private final String koreanName;

    CardType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static CardType fromCode(String code) {
        return switch (code) {
            case "N" -> CREDIT;
            case "Y" -> DEBIT;
            case "G" -> GIFT;
            default -> CREDIT;
        };
    }
}
