package com.onemorethink.domadosever.global.util;

import org.springframework.util.StringUtils;

public class CardUtils {

    /**
     * 카드번호 마스킹 처리
     * - 앞 6자리(BIN) + ****** + 뒤 4자리
     */
    public static String maskCardNumber(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            return "************";
        }

        if (cardNumber.length() < 16) {
            return "*".repeat(cardNumber.length());
        }

        return cardNumber.substring(0, 6) +
                "******" +
                cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * BIN 번호 추출
     */
    public static String extractBin(String cardNumber) {
        if (!StringUtils.hasText(cardNumber) || cardNumber.length() < 6) {
            return null;
        }
        return cardNumber.substring(0, 6);
    }

    /**
     * CVV 마스킹 처리
     */
    public static String maskCvv(String cvv) {
        if (!StringUtils.hasText(cvv)) {
            return "***";
        }
        return "*".repeat(cvv.length());
    }

    /**
     * 카드번호 포맷 검증
     */
    public static boolean isValidCardNumberFormat(String cardNumber) {
        System.out.println("Validating card number format: length=" + cardNumber.length());
        System.out.println("Is only digits: " + cardNumber.matches("^[0-9]+$"));
        System.out.println("Full regex match: " + cardNumber.matches("^[0-9]{16}$"));

        return StringUtils.hasText(cardNumber) &&
                cardNumber.matches("^[0-9]{16}$");
    }

    /**
     * CVV 포맷 검증
     */
    public static boolean isValidCvvFormat(String cvv) {
        return StringUtils.hasText(cvv) &&
                cvv.matches("^[0-9]{3,4}$");
    }
}