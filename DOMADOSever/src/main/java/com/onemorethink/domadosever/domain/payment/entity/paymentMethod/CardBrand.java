package com.onemorethink.domadosever.domain.payment.entity.paymentMethod;


public enum CardBrand {
    VISA("비자"),
    MASTERCARD("마스터"),
    JCB("JCB"),
    AMEX("아멕스"),
    DINERS("다이너스"),
    LOCAL("로컬"),
    UNIONPAY("은련"),
    UNKNOWN("알 수 없음");

    private final String koreanName;

    CardBrand(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static CardBrand fromCode(String code) {
        return switch (code) {
            case "V" -> VISA;
            case "M" -> MASTERCARD;
            case "J" -> JCB;
            case "A" -> AMEX;
            case "D" -> DINERS;
            case "L" -> LOCAL;
            case "C", "U", "G" -> UNIONPAY;
            default -> UNKNOWN;
        };
    }
}