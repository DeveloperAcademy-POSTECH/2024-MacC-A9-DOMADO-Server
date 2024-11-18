package com.onemorethink.domadosever.domain.rental.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HiBikeRequest {
    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "-90.0", message = "위도는 -90.0보다 커야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90.0보다 작아야 합니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "-180.0", message = "경도는 -180.0보다 커야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180.0보다 작아야 합니다")
    private Double longitude;

    @Builder
    public HiBikeRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}