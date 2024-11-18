package com.onemorethink.domadosever.domain.location.controller;

import com.onemorethink.domadosever.domain.location.dto.BikeLocationResponse;
import com.onemorethink.domadosever.domain.location.service.LocationService;
import com.onemorethink.domadosever.global.common.BaseResponse;
import com.onemorethink.domadosever.global.error.exception.InvalidParameterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Location API", description = "위치 기반 자전거 조회 API")
public class LocationController {
    private final LocationService locationService;

    @Operation(
            summary = "대여 가능한 자전거 위치 조회",
            description = """
                    사용자 위치 기준으로 주변의 자전거 대여소와 HiBike 위치를 조회합니다.
                    - 반경 내의 모든 허브와 스테이션 정보를 표시합니다.
                    - 각 허브와 스테이션의 대여 가능한 자전거 수를 제공합니다.
                    - HiBike로 등록된 자전거의 위치도 함께 제공합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BikeLocationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping("/bikes")
    public BaseResponse<BikeLocationResponse> getBikeLocations(
            @Parameter(description = "검색 중심점 위도", example = "36.014109")
            @RequestParam(name = "latitude")
            @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
            @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
            Double latitude,

            @Parameter(description = "검색 중심점 경도", example = "129.325666")
            @RequestParam(name = "longitude")
            @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
            @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
            Double longitude,

            @Parameter(description = "검색 반경(km)", example = "2")
            @RequestParam(name = "radius", defaultValue = "2")
            @Positive(message = "반경은 양수여야 합니다")
            @Max(value = 10, message = "반경은 10km를 초과할 수 없습니다")
            Double radius
    ) {
        log.debug("Retrieving bike locations - lat: {}, lon: {}, radius: {}km",
                latitude, longitude, radius);

        validateLocationParameters(latitude, longitude, radius);

        BikeLocationResponse response = locationService.findBikeLocations(latitude, longitude, radius);
        return BaseResponse.success(response);
    }

    private void validateLocationParameters(Double latitude, Double longitude, Double radius) {
        List<String> errors = new ArrayList<>();

        if (latitude == null) {
            errors.add("위도는 필수 입력값입니다.");
        }
        if (longitude == null) {
            errors.add("경도는 필수 입력값입니다.");
        }

        // 추가적인 비즈니스 검증 규칙이 있다면 여기에 구현 ( 위치에 대한 추가 검증 로직시 )

        if (!errors.isEmpty()) {
            throw new InvalidParameterException(errors);
        }
    }
}