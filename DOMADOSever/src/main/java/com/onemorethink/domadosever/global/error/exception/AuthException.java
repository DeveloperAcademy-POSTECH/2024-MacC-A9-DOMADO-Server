package com.onemorethink.domadosever.global.error.exception;

import com.onemorethink.domadosever.global.error.ErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends BusinessException {
    // 기본 생성자
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    // 커스텀 메시지를 위한 생성자
    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // 원인 예외를 포함하는 생성자
    public AuthException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}