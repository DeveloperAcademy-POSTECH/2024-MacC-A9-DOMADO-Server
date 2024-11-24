package com.onemorethink.domadosever.global.apns;

import com.onemorethink.domadosever.global.apns.dto.ApnsNotificationRequest;
import com.onemorethink.domadosever.global.apns.dto.DeviceTokenRequest;
import com.onemorethink.domadosever.global.apns.service.ApnsService;
import com.onemorethink.domadosever.global.apns.service.DeviceTokenService;
import com.onemorethink.domadosever.global.common.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final ApnsService apnsService;
    private final DeviceTokenService deviceTokenService;

    @PostMapping("/token")
    public BaseResponse<Void> registerToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeviceTokenRequest request) {
        String email = userDetails.getUsername();
        log.debug("Device token registration request - email: {}", email);

        deviceTokenService.registerToken(email, request.getToken());
        return BaseResponse.success();
    }

    @PostMapping("/send")
    public BaseResponse<Void> sendNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ApnsNotificationRequest request) {
        String email = userDetails.getUsername();
        log.debug("Push notification request - email: {}, title: {}", email, request.getTitle());

        apnsService.sendPushNotification(email, request);
        return BaseResponse.success();
    }
}