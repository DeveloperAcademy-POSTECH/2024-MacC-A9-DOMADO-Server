package com.onemorethink.domadosever.global.apns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTokenRequest {
    @NotBlank(message = "토큰은 필수입니다")
    @Pattern(regexp = "[0-9a-fA-F]{64}", message = "올바른 디바이스 토큰 형식이 아닙니다")
    private String token;
}
