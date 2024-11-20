package com.onemorethink.domadosever.domain.user.dto;

import com.onemorethink.domadosever.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

// 전체 로그인 응답
@Getter
@Builder
public class LoginResponse {
    private TokenResponse token;
    private AuthUserResponse user;

    @Getter
    @Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
    }

    public static LoginResponse of(String accessToken, String refreshToken, User user) {
        return LoginResponse.builder()
                .token(TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .build())
                .user(AuthUserResponse.from(user))
                .build();
    }
}