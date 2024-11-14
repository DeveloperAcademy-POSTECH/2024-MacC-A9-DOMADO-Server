package com.onemorethink.domadosever.global.error.exception;

import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.util.CardUtils;
import lombok.Getter;

@Getter
public class PaymentValidationException extends BusinessException {
    private final String maskedCardNumber;

    public PaymentValidationException(ErrorCode errorCode, String cardNumber) {
        super(errorCode);
        this.maskedCardNumber = CardUtils.maskCardNumber(cardNumber);
    }

    public PaymentValidationException(ErrorCode errorCode, String cardNumber, String message) {
        super(errorCode, message);
        this.maskedCardNumber = CardUtils.maskCardNumber(cardNumber);
    }
}