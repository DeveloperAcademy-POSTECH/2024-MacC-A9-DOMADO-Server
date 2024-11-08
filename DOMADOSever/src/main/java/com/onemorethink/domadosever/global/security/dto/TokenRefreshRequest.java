package com.onemorethink.domadosever.global.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
}
