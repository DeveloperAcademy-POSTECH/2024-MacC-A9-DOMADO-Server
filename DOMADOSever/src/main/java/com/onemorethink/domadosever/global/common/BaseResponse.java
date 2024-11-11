package com.onemorethink.domadosever.global.common;

import com.onemorethink.domadosever.global.error.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseResponse<T> {
    private boolean success;
    private T data;
    private Error error;

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.success = true;
        response.data = data;
        response.error = null;
        return response;
    }

    public static <T> BaseResponse<T> success() {
        return success(null);
    }

    public static <T> BaseResponse<T> failure(ErrorCode errorCode) {
        BaseResponse<T> response = new BaseResponse<>();
        response.success = false;
        response.data = null;
        response.error = new Error(errorCode);
        return response;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Error {
        private String code;
        private String message;

        Error(ErrorCode errorCode) {
            this.code = errorCode.getCode();
            this.message = errorCode.getMessage();
        }
    }
}

