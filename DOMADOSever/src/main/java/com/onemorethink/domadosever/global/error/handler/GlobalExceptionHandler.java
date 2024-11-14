package com.onemorethink.domadosever.global.error.handler;

import com.onemorethink.domadosever.global.common.BaseResponse;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.AuthException;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected BaseResponse<Object> handleException(Exception e) {
        log.error("Internal Server Error", e);
        return BaseResponse.failure(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    protected BaseResponse<Object> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        return BaseResponse.failure(e.getErrorCode());
    }

    @ExceptionHandler(AuthException.class)
    protected BaseResponse<Object> handleAuthException(AuthException e) {
        log.error("Auth Exception: {}", e.getMessage());
        return BaseResponse.failure(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected BaseResponse<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        ErrorCode errorCode = determineValidationErrorCode(e.getFieldErrors());
        return BaseResponse.failure(errorCode);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected BaseResponse<Object> handleConstraintViolationException(
            ConstraintViolationException e) {
        log.error("Constraint Violation Exception: {}", e.getMessage());
        return BaseResponse.failure(ErrorCode.INVALID_INPUT_VALUE);
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