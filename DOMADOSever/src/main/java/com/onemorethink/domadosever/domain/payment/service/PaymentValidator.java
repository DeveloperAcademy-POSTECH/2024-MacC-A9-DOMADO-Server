package com.onemorethink.domadosever.domain.payment.service;

import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.BinInfo;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethod;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.PaymentException;
import com.onemorethink.domadosever.global.error.exception.PaymentValidationException;
import com.onemorethink.domadosever.global.util.BinLoader;
import com.onemorethink.domadosever.global.util.CardUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class PaymentValidator {
    private static final int MAX_PAYMENT_METHODS = 5;
    private final BinLoader binLoader;

    @Autowired
    public PaymentValidator(BinLoader binLoader) {
        this.binLoader = binLoader;
        log.info("PaymentValidator initialized with BinLoader");
    }

    /**
     * 결제 수단 등록 전 카드 정보 검증
     */
    public CardValidationResult validateCardRegistration(
            String cardNumber,
            String cvv,
            String expiryMonth,
            String expiryYear,
            String cardHolderName,
            List<PaymentMethod> existingMethods) {

        // 최대 등록 가능 개수 검증
        if (existingMethods.size() >= MAX_PAYMENT_METHODS) {
            throw new PaymentException(ErrorCode.PAYMENT_METHOD_LIMIT_EXCEEDED);
        }

        // 중복 카드 검증
        if (existingMethods.stream()
                .anyMatch(method -> method.getCardNumber().equals(cardNumber))) {
            throw new PaymentException(ErrorCode.DUPLICATE_PAYMENT);
        }

        CardValidationResult result = new CardValidationResult();

        try {
            // 카드 번호 형식 검증
            if (!CardUtils.isValidCardNumberFormat(cardNumber)) {
                log.debug("Card number format validation failed");
                throw new PaymentValidationException(
                        ErrorCode.INVALID_CARD_FORMAT, cardNumber);
            }

            // Luhn 알고리즘 검증
            if (!validateLuhnAlgorithm(cardNumber)) {
                log.debug("Luhn algorithm validation failed");
                throw new PaymentValidationException(
                        ErrorCode.INVALID_CARD_CHECKSUM, cardNumber);
            }

            // BIN 검증
            String bin = CardUtils.extractBin(cardNumber);
            BinInfo binInfo = binLoader.getBinInfo(bin);
            if (binInfo == null) {
                throw new PaymentValidationException(
                        ErrorCode.INVALID_CARD_BIN, cardNumber);
            }
            log.debug("Found BIN info: issuer={}, brand={}, type={}",
                    binInfo.getIssuer(), binInfo.getBrand(), binInfo.getType());

            // 삭제/변경 예정 BIN 체크
            if (!binInfo.isValid()) {
                throw new PaymentValidationException(
                        ErrorCode.DISCONTINUED_CARD_BIN, cardNumber);
            }

            // CVV 검증
            if (!CardUtils.isValidCvvFormat(cvv)) {
                throw new PaymentValidationException(
                        ErrorCode.INVALID_CVV_FORMAT, cardNumber);
            }

            // 유효기간 검증
            if (!validateExpiry(expiryMonth, expiryYear)) {
                throw new PaymentValidationException(
                        ErrorCode.EXPIRED_CARD, cardNumber);
            }

            // 카드 소유자 이름 검증
            if (!validateCardHolderName(cardHolderName)) {
                throw new PaymentValidationException(
                        ErrorCode.INVALID_INPUT_VALUE, cardNumber, "카드 소유자 이름이 올바르지 않습니다");
            }

            result.setValid(true);
            result.setBinInfo(binInfo);
            result.setMaskedCardNumber(CardUtils.maskCardNumber(cardNumber));
            result.setCardCompany(binInfo.getIssuer());
            result.setCardType(binInfo.getType().getKoreanName());
            result.setCardBrand(binInfo.getBrand().getKoreanName());

        } catch (PaymentValidationException e) {
            result.setValid(false);
            result.addError(e.getMessage());
        }

        return result;
    }

    /**
     * 결제 전 카드 유효성 검증
     */
    public void validatePayment(String cardNumber, BigDecimal amount) {
        // BIN 정보 조회 및 검증
        String bin = CardUtils.extractBin(cardNumber);
        BinInfo binInfo = binLoader.getBinInfo(bin);
        if (binInfo == null) {
            throw new PaymentValidationException(
                    ErrorCode.INVALID_CARD_BIN, cardNumber);
        }

        // 금액 검증
        validateAmount(amount);

        // 한도 검증
        validatePaymentLimit(binInfo, amount);
    }

    private boolean validateLuhnAlgorithm(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    private boolean validateExpiry(String month, String year) {
        try {
            int expiryMonth = Integer.parseInt(month);
            int expiryYear = Integer.parseInt(year);

            if (expiryMonth < 1 || expiryMonth > 12) {
                return false;
            }

            LocalDate now = LocalDate.now();
            LocalDate expiryDate = LocalDate.of(2000 + expiryYear, expiryMonth, 1)
                    .plusMonths(1).minusDays(1);

            return !expiryDate.isBefore(now);
        } catch (NumberFormatException | DateTimeException e) {
            return false;
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new PaymentException(
                    ErrorCode.INVALID_PAYMENT_AMOUNT,
                    "결제 금액이 null입니다");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(
                    ErrorCode.INVALID_PAYMENT_AMOUNT,
                    "결제 금액은 0보다 커야 합니다");
        }

        if (amount.scale() > 0) {
            throw new PaymentException(
                    ErrorCode.INVALID_PAYMENT_AMOUNT,
                    "결제 금액은 정수여야 합니다");
        }
    }

    private void validatePaymentLimit(BinInfo binInfo, BigDecimal amount) {
        // 일일 한도 검증
        BigDecimal dailyTotal = getDailyPaymentTotal(binInfo);
        BigDecimal dailyLimit = getDailyLimit(binInfo);
        if (dailyTotal.add(amount).compareTo(dailyLimit) > 0) {
            throw new PaymentException(
                    ErrorCode.DAILY_PAYMENT_LIMIT_EXCEEDED,
                    String.format("일일 결제 한도 초과 (한도: %s원)", dailyLimit));
        }

        // 월간 한도 검증
        BigDecimal monthlyTotal = getMonthlyPaymentTotal(binInfo);
        BigDecimal monthlyLimit = getMonthlyLimit(binInfo);
        if (monthlyTotal.add(amount).compareTo(monthlyLimit) > 0) {
            throw new PaymentException(
                    ErrorCode.MONTHLY_PAYMENT_LIMIT_EXCEEDED,
                    String.format("월간 결제 한도 초과 (한도: %s원)", monthlyLimit));
        }
    }

    private BigDecimal getDailyLimit(BinInfo binInfo) {
        return switch (binInfo.getType()) {
            case CREDIT -> binInfo.isPersonal() ?
                    new BigDecimal("10000000") : new BigDecimal("50000000");
            case DEBIT -> binInfo.isPersonal() ?
                    new BigDecimal("5000000") : new BigDecimal("20000000");
            case GIFT -> new BigDecimal("1000000");
        };
    }

    private BigDecimal getMonthlyLimit(BinInfo binInfo) {
        return getDailyLimit(binInfo).multiply(new BigDecimal("30"));
    }

    // TODO: 실제 구현에서는 결제 이력을 조회하여 계산
    private BigDecimal getDailyPaymentTotal(BinInfo binInfo) {
        return BigDecimal.ZERO;
    }

    private BigDecimal getMonthlyPaymentTotal(BinInfo binInfo) {
        return BigDecimal.ZERO;
    }

    private boolean validateCardHolderName(String name) {
        return StringUtils.hasText(name) && name.length() >= 2 && name.length() <= 50;
    }

    @Getter
    @Setter
    public static class CardValidationResult {
        private boolean isValid;
        private List<String> errors = new ArrayList<>();
        private BinInfo binInfo;
        private String maskedCardNumber;
        private String cardCompany;
        private String cardType;
        private String cardBrand;

        public void addError(String error) {
            this.errors.add(error);
        }
    }
}
