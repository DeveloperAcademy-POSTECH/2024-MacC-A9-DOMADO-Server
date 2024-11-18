package com.onemorethink.domadosever.domain.rental.dto;

import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "HiBike 응답 데이터")
@Getter
@Builder
public class HiBikeResponse {
    @Schema(
            description = "대여 ID",
            example = "12345",
            required = true
    )
    private Long rentalId;

    @Schema(
            description = "자전거 ID",
            example = "67890",
            required = true
    )
    private Long bikeId;

    @Schema(
            description = "자전거 상태",
            example = "TEMPORARY_LOCKED",
            required = true,
            enumAsRef = true
    )
    private BikeStatus bikeStatus;

    @Schema(
            description = "HiBike 상태",
            example = "AVAILABLE_FOR_RENT",
            required = true,
            enumAsRef = true
    )
    private HiBikeStatus hiBikeStatus;

    @Schema(
            description = "응답 메시지",
            example = "자전거가 성공적으로 HiBike로 전환되었습니다.",
            required = true
    )
    private String message;
}