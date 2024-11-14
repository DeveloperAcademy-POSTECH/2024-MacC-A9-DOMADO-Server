package com.onemorethink.domadosever.domain.rental.dto;

import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "자전거 대여 일시정지 응답")
@Getter
@Builder
public class RentalPauseResponse {
    @Schema(description = "대여 ID", example = "12345")
    private Long rentalId;

    @Schema(description = "자전거 상태", example = "PAUSED")
    private BikeStatus bikeStatus;

    @Schema(description = "일시정지 시작 시간", example = "2024-03-13T14:30:00")
    private LocalDateTime pauseTime;

    @Schema(description = "누적 일시정지 시간(분)", example = "30")
    private Integer totalPauseMinutes;

    @Schema(description = "응답 메시지", example = "자전거가 성공적으로 일시정지되었습니다")
    private String message;
}