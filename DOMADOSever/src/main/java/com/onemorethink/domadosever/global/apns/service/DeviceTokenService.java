package com.onemorethink.domadosever.global.apns.service;

import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.repository.UserRepository;
import com.onemorethink.domadosever.global.apns.entity.DeviceToken;
import com.onemorethink.domadosever.global.apns.repository.DeviceTokenRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.ApnsException;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DeviceTokenService {
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public void registerToken(String email, String token) {
        // 토큰 형식 검증
        validateTokenFormat(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 등록된 토큰이 있다면 재사용하지 않음
        if (deviceTokenRepository.existsByTokenAndActiveIsTrue(token)) {
            log.debug("Token already exists and active: {}", token);
            return;
        }

        DeviceToken deviceToken = DeviceToken.builder()
                .user(user)
                .token(token)
                .build();

        deviceTokenRepository.save(deviceToken);
        log.info("Device token registered for user: {}", email);
    }

    public List<String> getActiveTokensByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<String> tokens = deviceTokenRepository.findByUserAndActiveIsTrue(user).stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            throw new ApnsException(ErrorCode.DEVICE_TOKEN_NOT_FOUND);
        }

        return tokens;
    }

    public void deactivateToken(String token) {
        deviceTokenRepository.findByTokenAndActiveIsTrue(token)
                .ifPresent(deviceToken -> {
                    deviceToken.deactivate();
                    log.info("Device token deactivated: {}", token);
                });
    }

    private void validateTokenFormat(String token) {
        if (!token.matches("[0-9a-fA-F]{64}")) {
            throw new ApnsException(ErrorCode.INVALID_DEVICE_TOKEN_FORMAT);
        }
    }
}