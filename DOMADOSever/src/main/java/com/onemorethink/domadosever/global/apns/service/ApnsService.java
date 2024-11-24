package com.onemorethink.domadosever.global.apns.service;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.onemorethink.domadosever.global.apns.dto.ApnsNotificationRequest;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.ApnsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApnsService {
    private final ApnsClient apnsClient;
    private final DeviceTokenService deviceTokenService;

    @Value("${apns.bundle-id}")
    private String bundleId;

    public void sendPushNotification(String email, ApnsNotificationRequest request) {
        try {
            List<String> tokens = deviceTokenService.getActiveTokensByEmail(email);

            for (String token : tokens) {
                try {
                    sendSingleNotification(token, request);
                } catch (Exception e) {
                    log.error("Failed to send push notification to token: {}", token, e);
                    if (isInvalidTokenError(e)) {
                        deviceTokenService.deactivateToken(token);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in push notification process", e);
            throw new ApnsException(ErrorCode.APNS_SEND_ERROR,
                    "푸시 알림 전송 중 오류가 발생했습니다", e);
        }
    }

    private void sendSingleNotification(String token, ApnsNotificationRequest request) {
        try {
            String payload = createPayload(request);
            SimpleApnsPushNotification notification = new SimpleApnsPushNotification(
                    token,
                    bundleId,
                    payload
            );

            CompletableFuture<PushNotificationResponse<SimpleApnsPushNotification>> future =
                    apnsClient.sendNotification(notification);

            PushNotificationResponse<SimpleApnsPushNotification> response =
                    future.get(10, TimeUnit.SECONDS);

            if (response.isAccepted()) {
                log.info("Push notification sent successfully to device: {}", token);
            } else {
                String reason = response.getRejectionReason().orElse("Unknown reason");
                log.error("Failed to send push notification. Reason: {}", reason);
                handlePushNotificationError(token, reason);
            }

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new ApnsException(ErrorCode.APNS_SEND_ERROR, "푸시 알림 전송 중 오류가 발생했습니다", e);
        }
    }

    private String createPayload(ApnsNotificationRequest request) {
        try {
            JSONObject payload = new JSONObject();
            JSONObject aps = new JSONObject();
            JSONObject alert = new JSONObject();

            // alert 구성
            alert.put("title", request.getTitle());
            alert.put("body", request.getBody());

            // aps 구성
            aps.put("alert", alert);
            aps.put("sound", request.getSound());
            aps.put("badge", request.getBadge());

            // 메인 payload에 aps 추가
            payload.put("aps", aps);

            // 커스텀 데이터 추가
            if (request.getData() != null) {
                for (Map.Entry<String, Object> entry : request.getData().entrySet()) {
                    payload.put(entry.getKey(), entry.getValue());
                }
            }

            String jsonPayload = payload.toString();
            if (jsonPayload.getBytes(StandardCharsets.UTF_8).length > 4096) {
                throw new ApnsException(ErrorCode.APNS_PAYLOAD_TOO_LARGE,
                        "푸시 알림 페이로드가 최대 크기(4KB)를 초과했습니다");
            }

            return jsonPayload;
        } catch (JSONException e) {
            throw new ApnsException(ErrorCode.APNS_PAYLOAD_TOO_LARGE,
                    "푸시 알림 페이로드 생성 중 오류가 발생했습니다", e);
        }
    }

    private void handlePushNotificationError(String token, String reason) {
        if (isInvalidTokenReason(reason)) {
            deviceTokenService.deactivateToken(token);
            throw new ApnsException(ErrorCode.APNS_INVALID_TOKEN,
                    "유효하지 않은 디바이스 토큰입니다: " + reason);
        }
        throw new ApnsException(ErrorCode.APNS_SEND_ERROR,
                "푸시 알림 전송 실패: " + reason);
    }

    private boolean isInvalidTokenError(Exception e) {
        return e.getMessage() != null && (
                e.getMessage().contains("InvalidToken") ||
                        e.getMessage().contains("BadDeviceToken")
        );
    }

    private boolean isInvalidTokenReason(String reason) {
        return "BadDeviceToken".equals(reason) ||
                "Unregistered".equals(reason);
    }
}