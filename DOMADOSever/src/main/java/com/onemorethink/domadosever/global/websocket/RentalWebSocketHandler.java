package com.onemorethink.domadosever.global.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onemorethink.domadosever.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// WebSocket 핸들러
@Component
@Slf4j
@RequiredArgsConstructor
public class RentalWebSocketHandler extends TextWebSocketHandler {
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = getEmailFromSession(session);
        if (email == null) {
            session.close();
            return;
        }

        // 기존 세션 처리
        WebSocketSession existingSession = sessions.get(email);
        if (existingSession != null && existingSession.isOpen()) {
            existingSession.close();
        }

        sessions.put(email, session);

        // 연결 성공 메시지 전송
        sendMessage(session, WebSocketMessage.success(
                WebSocketMessageType.CONNECTED,
                Map.of("message", "WebSocket 연결이 성공적으로 수립되었습니다.")
        ));

        log.info("WebSocket 연결 수립 - 사용자: {}", email);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String email = getEmailFromSession(session);
        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.path("type").asText();

            log.debug("메시지 수신 - 사용자: {}, 타입: {}", email, type);

            // 메시지 타입별 처리
            handleMessageByType(email, type, jsonNode);

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생 - 사용자: {}", email, e);
            try {
                sendMessage(session, WebSocketMessage.error(
                        WebSocketMessageType.ERROR,
                        ErrorCode.INVALID_MESSAGE_FORMAT
                ));
            } catch (IOException ex) {
                log.error("에러 메시지 전송 실패", ex);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String email = getEmailFromSession(session);
        sessions.remove(email);
        log.info("WebSocket 연결 종료 - 사용자: {}, 상태: {}", email, status);
    }

    public void sendMessage(String email, WebSocketMessage<?> message) throws IOException {
        WebSocketSession session = sessions.get(email);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        String payload = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(payload));
    }

    private String getEmailFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get("email");
    }

    private void handleMessageByType(String email, String type, JsonNode messageData) {
        // 메시지 타입별 처리 로직 구현
    }
}