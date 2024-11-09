package com.onemorethink.domadosever.global.error.handler;

import com.onemorethink.domadosever.global.common.BaseResponse;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.AuthException;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected BaseResponse<Object> handleException(Exception e) {
        log.error("Internal Server Error", e);
        return new BaseResponse<>(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    protected BaseResponse<Object> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        return new BaseResponse<>(e.getErrorCode());
    }

    @ExceptionHandler(AuthException.class)
    protected BaseResponse<Object> handleAuthException(AuthException e) {
        log.error("Auth Exception: {}", e.getMessage());
        return new BaseResponse<>(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected BaseResponse<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        ErrorCode errorCode = determineValidationErrorCode(e.getFieldErrors());
        return new BaseResponse<>(errorCode);
    }

    private ErrorCode determineValidationErrorCode(List<FieldError> fieldErrors) {
        if (fieldErrors.isEmpty()) {
            return ErrorCode.INVALID_INPUT_VALUE;
        }

        FieldError error = fieldErrors.get(0);
        String field = error.getField().toLowerCase();

        return switch (field) {
            case "email" -> ErrorCode.INVALID_EMAIL_FORMAT;
            case "password" -> ErrorCode.INVALID_PASSWORD_FORMAT;
            case "phone" -> ErrorCode.INVALID_PHONE_FORMAT;
            default -> ErrorCode.INVALID_INPUT_VALUE;
        };
    }
}