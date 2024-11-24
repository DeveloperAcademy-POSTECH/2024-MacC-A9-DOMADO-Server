package com.onemorethink.domadosever.global.error.exception;

import com.onemorethink.domadosever.global.error.ErrorCode;
import lombok.Getter;

@Getter
public class ApnsException extends BusinessException {
    public ApnsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ApnsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ApnsException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}