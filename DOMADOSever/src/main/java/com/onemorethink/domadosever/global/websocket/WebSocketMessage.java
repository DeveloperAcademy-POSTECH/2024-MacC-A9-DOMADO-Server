package com.onemorethink.domadosever.global.websocket;

import com.onemorethink.domadosever.global.error.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

// WebSocket 메시지 기본 포맷
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WebSocketMessage<T> {
    private String type;        // 메시지 타입
    private T payload;          // 실제 데이터
    private boolean success;    // 성공/실패 여부
    private Error error;        // 에러 정보
    private String timestamp;   // 메시지 발생 시간

    // 성공 메시지 생성
    public static <T> WebSocketMessage<T> success(String type, T payload) {
        return WebSocketMessage.<T>builder()
                .type(type)
                .payload(payload)
                .success(true)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 에러 메시지 생성
    public static <T> WebSocketMessage<T> error(String type, ErrorCode errorCode) {
        return WebSocketMessage.<T>builder()
                .type(type)
                .success(false)
                .error(new Error(errorCode))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    private static class Error {
        private String code;
        private String message;

        Error(ErrorCode errorCode) {
            this.code = errorCode.getCode();
            this.message = errorCode.getMessage();
        }
    }
}