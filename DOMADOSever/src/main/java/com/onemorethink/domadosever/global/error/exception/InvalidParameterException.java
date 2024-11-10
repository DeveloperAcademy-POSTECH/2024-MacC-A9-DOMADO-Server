package com.onemorethink.domadosever.global.error.exception;

import com.onemorethink.domadosever.global.error.ErrorCode;
import lombok.Getter;

import java.util.List;

@ Getter
public class InvalidParameterException extends BusinessException {
    private final List<String> errors;

    public InvalidParameterException(List<String> errors) {
        super(ErrorCode.INVALID_INPUT_VALUE, "잘못된 입력값이 있습니다");
        this.errors = errors;
    }
}