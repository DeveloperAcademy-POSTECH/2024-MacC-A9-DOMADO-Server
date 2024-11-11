package com.onemorethink.domadosever.domain.rental.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자전거 대여 요청")
public class RentBikeRequest {

    @Schema(description = "자전거 QR 코드", example = "BIKE001")
    @NotBlank(message = "QR 코드는 필수입니다")
    private String qrCode;
}