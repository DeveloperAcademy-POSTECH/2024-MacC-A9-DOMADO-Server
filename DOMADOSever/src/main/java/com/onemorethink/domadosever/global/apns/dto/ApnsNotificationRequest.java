package com.onemorethink.domadosever.global.apns.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ApnsNotificationRequest {
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String body;

    private Map<String, Object> data;

    @Builder.Default
    private String sound = "default";

    @Builder.Default
    private int badge = 1;
}