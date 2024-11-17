package com.onemorethink.domadosever.domain.rental.controller;

import com.onemorethink.domadosever.domain.rental.dto.HiBikeRequest;
import com.onemorethink.domadosever.domain.rental.dto.HiBikeResponse;
import com.onemorethink.domadosever.domain.rental.service.HiBikeService;
import com.onemorethink.domadosever.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rentals/{rentalId}/hibike")
@RequiredArgsConstructor
@Tag(name = "HiBike API", description = "HiBike 관련 API")
public class HiBikeController {
    private final HiBikeService hiBikeService;

    @Operation(
            summary = "HiBike 전환 요청",
            description = """
                    일시잠금 상태의 자전거를 HiBike로 전환합니다.
                    - 진행 중인 대여인지 확인합니다.
                    - 자전거가 일시잠금 상태인지 확인합니다.
                    - 이미 HiBike 상태가 아닌지 확인합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HiBike 전환 성공",
                    content = @Content(schema = @Schema(implementation = HiBikeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대여 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping
    public BaseResponse<HiBikeResponse> makeHiBike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer rentalId,
            @Valid @RequestBody HiBikeRequest request
    ) {
        String email = userDetails.getUsername();
        log.debug("HiBike request - email: {}, rentalId: {}", email, rentalId);

        HiBikeResponse response = hiBikeService.makeHiBike(email, rentalId, request);
        return BaseResponse.success(response);
    }

    @Operation(
            summary = "HiBike 취소 요청",
            description = """
                    HiBike 상태를 취소합니다.
                    - 진행 중인 대여인지 확인합니다.
                    - 자전거가 HiBike 상태인지 확인합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HiBike 취소 성공",
                    content = @Content(schema = @Schema(implementation = HiBikeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대여 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @DeleteMapping
    public BaseResponse<HiBikeResponse> cancelHiBike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer rentalId
    ) {
        String email = userDetails.getUsername();
        log.debug("HiBike cancel request - email: {}, rentalId: {}", email, rentalId);

        HiBikeResponse response = hiBikeService.cancelHiBike(email, rentalId);
        return BaseResponse.success(response);
    }
}