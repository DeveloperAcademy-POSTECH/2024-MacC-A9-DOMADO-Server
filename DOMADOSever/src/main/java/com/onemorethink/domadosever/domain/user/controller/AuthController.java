package com.onemorethink.domadosever.domain.user.controller;

import com.onemorethink.domadosever.domain.user.dto.LoginRequest;
import com.onemorethink.domadosever.domain.user.dto.LogoutRequest;
import com.onemorethink.domadosever.domain.user.dto.RegisterRequest;
import com.onemorethink.domadosever.domain.user.dto.UserResponse;
import com.onemorethink.domadosever.domain.user.service.AuthenticationService;
import com.onemorethink.domadosever.global.common.BaseResponse;
import com.onemorethink.domadosever.global.security.dto.TokenRefreshRequest;
import com.onemorethink.domadosever.global.security.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication", description = "인증 관련 API")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 이메일",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/register")
    public BaseResponse<Void> register(
            @Valid @RequestBody
            @Parameter(description = "회원가입 정보", required = true)
            RegisterRequest request
    ) {
        authenticationService.register(request);
        return BaseResponse.success();
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하여 액세스 토큰과 리프레시 토큰을 발급받습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(
            @Valid @RequestBody
            @Parameter(description = "로그인 정보", required = true)
            LoginRequest request,
            @Parameter(hidden = true)
            HttpServletRequest httpRequest
    ) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = extractClientIp(httpRequest);

        TokenResponse tokenResponse = authenticationService.login(request, userAgent, clientIp);
        return BaseResponse.success(tokenResponse);
    }

    @Operation(
            summary = "토큰 갱신",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 리프레시 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/refresh")
    public BaseResponse<TokenResponse> refreshToken(
            @Valid @RequestBody
            @Parameter(description = "리프레시 토큰 정보", required = true)
            TokenRefreshRequest request,
            @Parameter(hidden = true)
            HttpServletRequest httpRequest
    ) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = extractClientIp(httpRequest);

        TokenResponse tokenResponse = authenticationService.refreshToken(
                request.getRefreshToken(),
                userAgent,
                clientIp
        );
        return BaseResponse.success(tokenResponse);
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 사용 중인 리프레시 토큰을 무효화합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/logout")
    public BaseResponse<Void> logout(
            @Valid @RequestBody
            @Parameter(description = "로그아웃 정보", required = true)
            LogoutRequest request
    ) {
        authenticationService.logout(request.getRefreshToken());
        return BaseResponse.success();
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/me")
    public BaseResponse<UserResponse> getCurrentUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse userResponse = authenticationService.getCurrentUser(userDetails.getUsername());
        return BaseResponse.success(userResponse);
    }

    private String extractClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}