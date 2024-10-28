package com.onemorethink.domadosever.global.common;

import com.onemorethink.domadosever.global.error.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private T data;
    private Error error;

    // 성공 응답을 위한 생성자
    public BaseResponse(T data) {
        this.success = true;
        this.data = data;
        this.error = null;
    }

    // 실패 응답을 위한 생성자
    public BaseResponse(ErrorCode errorCode) {
        this.success = false;
        this.data = null;
        this.error = new Error(errorCode);
    }

    @Getter
    @NoArgsConstructor
    private static class Error {
        private String code;
        private String message;

        Error(ErrorCode errorCode) {
            this.code = errorCode.getCode();
            this.message = errorCode.getMessage();
        }
    }
}
