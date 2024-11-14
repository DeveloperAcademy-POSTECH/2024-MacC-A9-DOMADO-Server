package com.onemorethink.domadosever.domain.rental.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "자전거 일시잠금 해제 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RentalResumeRequest {
    @Schema(
            description = "현재 위치 위도",
            example = "37.5665",
            minimum = "-90",
            maximum = "90"
    )
    @NotNull(message = "현재 위치(위도)는 필수입니다")
    @DecimalMin(value = "-90", message = "위도는 -90도 이상이어야 합니다")
    @DecimalMax(value = "90", message = "위도는 90도 이하여야 합니다")
    private Double latitude;

    @Schema(
            description = "현재 위치 경도",
            example = "126.9780",
            minimum = "-180",
            maximum = "180"
    )
    @NotNull(message = "현재 위치(경도)는 필수입니다")
    @DecimalMin(value = "-180", message = "경도는 -180도 이상이어야 합니다")
    @DecimalMax(value = "180", message = "경도는 180도 이하여야 합니다")
    private Double longitude;
}