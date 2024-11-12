package com.onemorethink.domadosever.domain.rental.dto;

import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "자전거 잠금 해제 응답")
@Getter
@Builder
public class RentalResumeResponse {
    @Schema(description = "대여 ID", example = "12345")
    private Long rentalId;

    @Schema(description = "자전거 상태", example = "IN_USE")
    private BikeStatus bikeStatus;

    @Schema(description = "대여 잠금 해제 시간", example = "2024-03-13T15:00:00")
    private LocalDateTime resumeTime;

    @Schema(description = "이번 일시정지 동안의 정지 시간(분)", example = "30")
    private Integer pauseMinutes;

    @Schema(description = "응답 메시지", example = "자전거 대여가 성공적으로 재개되었습니다")
    private String message;
}