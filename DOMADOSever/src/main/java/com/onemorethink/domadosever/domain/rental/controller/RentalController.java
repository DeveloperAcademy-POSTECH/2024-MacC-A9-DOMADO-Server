package com.onemorethink.domadosever.domain.rental.controller;

import com.onemorethink.domadosever.domain.rental.dto.*;
import com.onemorethink.domadosever.domain.rental.service.RentalService;
import com.onemorethink.domadosever.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @Operation(
            summary = "자전거 일시 잠금",
            description = """
                    대여 중인 자전거를 일시정지합니다.
                    - 사용자 인증 정보를 확인합니다.
                    - 해당 대여 건에 대한 권한을 확인합니다.
                    - 현재 위치 정보를 기반으로 주차 가능 구역인지 확인합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "일시잠금 성공",
                    content = @Content(schema = @Schema(implementation = RentalPauseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (주차 불가 구역 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (본인의 대여가 아닌 경우)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대여 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @Parameter(name = "rentalId", description = "Rental ID", required = true)
    @PostMapping("/{rentalId}/pause")
    public BaseResponse<RentalPauseResponse> pauseBike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("rentalId")Integer rentalId,
            @Valid @RequestBody RentalPauseRequest request
    ) {
        RentalPauseResponse response = rentalService.pauseBike(
                userDetails.getUsername(),
                rentalId,
                request
        );
        return BaseResponse.success(response);
    }

    @Operation(
            summary = "자전거 일시잠금 해제",
            description = """
                    자전거 일시 잠금을 해제합니다..
                    - 사용자 인증 정보를 확인합니다.
                    - 해당 대여 건에 대한 권한을 확인합니다.
                    - 자전거 잠금 상태를 확인합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "잠금해제 성공",
                    content = @Content(schema = @Schema(implementation = RentalResumeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (주차 불가 구역 등)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (본인의 대여가 아닌 경우)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대여 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @Parameter(name = "rentalId", description = "Rental ID", required = true)
    @PostMapping("/{rentalId}/resume")
    public BaseResponse<RentalResumeResponse> resumeBike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("rentalId") Integer rentalId,
            @Valid @RequestBody RentalResumeRequest request
    ) {
        RentalResumeResponse response = rentalService.resumeBike(
                userDetails.getUsername(),
                rentalId,
                request
        );
        return BaseResponse.success(response);
    }
}
