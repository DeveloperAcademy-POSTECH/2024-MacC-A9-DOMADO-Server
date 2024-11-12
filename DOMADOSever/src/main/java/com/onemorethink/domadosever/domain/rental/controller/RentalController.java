package com.onemorethink.domadosever.domain.rental.controller;

import com.onemorethink.domadosever.domain.rental.dto.RentalResponse;
import com.onemorethink.domadosever.domain.rental.service.RentalService;
import com.onemorethink.domadosever.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@Tag(name = "Rental API", description = "자전거 대여 API")
public class RentalController {
    private final RentalService rentalService;

    @Operation(
            summary = "자전거 대여 요청",
            description = """
                    QR 코드를 스캔하여 자전거 대여를 요청합니다.
                    - 사용자 상태 및 자격을 검증합니다.
                    - 자전거 상태 및 대여 가능 여부를 확인합니다.
                    - 결제 수단 존재 여부를 확인합니다.
                    - 진행 중인 대여가 없는지 확인합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "대여 성공",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 자전거를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping("/rent")
    public BaseResponse<RentalResponse> rentBike(
            @AuthenticationPrincipal UserDetails userDetails,

            @Parameter(description = "자전거 QR 코드", example = "BIKE123456")
            @RequestParam(name = "qrCode")
            @NotBlank(message = "QR 코드는 필수입니다")
            String qrCode
    ) {
        String email = userDetails.getUsername();
        log.debug("Rental request - email: {}, qrCode: {}", email, qrCode);

        RentalResponse response = rentalService.rentBikeByEmail(email, qrCode);
        return BaseResponse.success(response);
    }
}