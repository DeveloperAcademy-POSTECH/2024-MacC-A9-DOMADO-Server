package com.onemorethink.domadosever.global.error.handler;

import com.onemorethink.domadosever.global.common.BaseResponse;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<BaseResponse<Object>> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new BaseResponse<>(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<BaseResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new BaseResponse<>(ErrorCode.INVALID_INPUT_VALUE));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<BaseResponse<Object>> handleException(Exception e) {
        log.error("Internal Server Error: {}", e.getMessage());
        return ResponseEntity
                .internalServerError()
                .body(new BaseResponse<>(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}