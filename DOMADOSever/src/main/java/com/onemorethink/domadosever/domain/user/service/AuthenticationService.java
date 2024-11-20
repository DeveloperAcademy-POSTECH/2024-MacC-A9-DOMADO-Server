package com.onemorethink.domadosever.domain.user.service;

import com.onemorethink.domadosever.domain.user.dto.LoginRequest;
import com.onemorethink.domadosever.domain.user.dto.LoginResponse;
import com.onemorethink.domadosever.domain.user.dto.RegisterRequest;
import com.onemorethink.domadosever.domain.user.dto.UserResponse;
import com.onemorethink.domadosever.domain.user.entity.Role;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.entity.UserStatus;
import com.onemorethink.domadosever.domain.user.repository.RoleRepository;
import com.onemorethink.domadosever.domain.user.repository.UserRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.AuthException;
import com.onemorethink.domadosever.global.security.dto.TokenResponse;
import com.onemorethink.domadosever.global.security.entity.RefreshToken;
import com.onemorethink.domadosever.global.security.jwt.JwtTokenProvider;
import com.onemorethink.domadosever.global.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public LoginResponse login(LoginRequest request, String userAgent, String clientIp) {
        try {
            // 이메일 형식 검증
            if (!isValidEmail(request.getEmail())) {
                throw new AuthException(ErrorCode.INVALID_EMAIL_FORMAT);
            }

            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 사용자 상태 확인
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

            validateUserStatus(user);

            // 기존의 모든 유효한 리프레시 토큰 폐기
            revokeAllUserTokens(user);

            // 새로운 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication, userAgent, clientIp);

            return LoginResponse.of(accessToken, refreshToken, user);

        } catch (BadCredentialsException e) {
            throw new AuthException(ErrorCode.INVALID_PASSWORD);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw new AuthException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken, String userAgent, String clientIp) {
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
            }

            RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));

            validateRefreshToken(storedToken);
            validateClientInfo(storedToken, userAgent, clientIp);

            User user = storedToken.getUser();
            validateUserStatus(user);

            revokeAllUserTokens(user);

            Authentication authentication = createAuthentication(user);
            String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication, userAgent, clientIp);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .build();

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new AuthException(ErrorCode.TOKEN_REFRESH_FAILED);
        }
    }

    @Transactional
    public void register(RegisterRequest request) {
        try {
            validateRegistrationInput(request);

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthException(ErrorCode.DUPLICATE_EMAIL);
            }

            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new AuthException(ErrorCode.ROLE_NOT_FOUND));

            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .phone(request.getPhone())
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(userRole))
                    .build();

            userRepository.save(user);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            throw new AuthException(ErrorCode.REGISTRATION_FAILED);
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));

            revokeAllUserTokens(token.getUser());
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new AuthException(ErrorCode.LOGOUT_FAILED);
        }
    }

    public UserResponse getCurrentUser(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

            validateUserStatus(user);
            return UserResponse.from(user);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get current user info for email: {}", email, e);
            throw new AuthException(ErrorCode.USER_RETRIEVAL_FAILED);
        }
    }

    private void validateRegistrationInput(RegisterRequest request) {
        if (!isValidEmail(request.getEmail())) {
            throw new AuthException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        if (!isValidPassword(request.getPassword())) {
            throw new AuthException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
        if (!isValidPhoneNumber(request.getPhone())) {
            throw new AuthException(ErrorCode.INVALID_PHONE_FORMAT);
        }
    }

    private void validateRefreshToken(RefreshToken token) {
        if (token.isExpired()) {
            log.warn("Attempt to use expired refresh token");
            throw new AuthException(ErrorCode.EXPIRED_TOKEN);
        }
        if (token.isRevoked()) {
            log.warn("Attempt to use revoked refresh token");
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void validateClientInfo(RefreshToken token, String userAgent, String clientIp) {
        if (!token.getUserAgent().equals(userAgent) || !token.getClientIp().equals(clientIp)) {
            log.warn("Suspicious token reuse detected. Token ID: {}, IP: {}, UserAgent: {}",
                    token.getId(), clientIp, userAgent);
            token.revoke();
            refreshTokenRepository.save(token);
            throw new AuthException(ErrorCode.SUSPICIOUS_TOKEN_REUSE);
        }
    }

    private void validateUserStatus(User user) {
        switch (user.getStatus()) {
            case LOCKED -> throw new AuthException(ErrorCode.ACCOUNT_LOCKED);
            case SUSPENDED -> throw new AuthException(ErrorCode.ACCOUNT_SUSPENDED);
            case BLOCKED -> throw new AuthException(ErrorCode.ACCOUNT_BLOCKED);
            case WITHDRAWN -> throw new AuthException(ErrorCode.ACCOUNT_WITHDRAWN);
            case ACTIVE -> {} // 정상 상태
            default -> throw new AuthException(ErrorCode.INVALID_USER_STATUS);
        }
    }

    private void revokeAllUserTokens(User user) {
        List<RefreshToken> validUserTokens = refreshTokenRepository.findAllValidTokensByUser(
                user.getId(),
                LocalDateTime.now()
        );

        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
            });
            log.debug("Revoked {} refresh tokens for user: {}", validUserTokens.size(), user.getEmail());
        }
    }

    private Authentication createAuthentication(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                        .collect(Collectors.toList()))
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPassword(String password) {
        return password != null &&
                password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$");
    }
}