package com.onemorethink.domadosever.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
}