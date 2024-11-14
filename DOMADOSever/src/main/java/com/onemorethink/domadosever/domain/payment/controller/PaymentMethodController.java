package com.onemorethink.domadosever.domain.payment.controller;

import com.onemorethink.domadosever.domain.payment.dto.PaymentMethodRequest;
import com.onemorethink.domadosever.domain.payment.dto.PaymentMethodResponse;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethod;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethodStatus;
import com.onemorethink.domadosever.domain.payment.service.PaymentMethodService;
import com.onemorethink.domadosever.global.common.BaseResponse;
import com.onemorethink.domadosever.global.util.CardUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Method API", description = "결제 수단 관리 API")
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @Operation(
            summary = "결제 수단 등록",
            description = """
                    새로운 결제 수단을 등록합니다.
                    - 카드 정보 유효성을 검증합니다.
                    - 첫 번째 등록 카드는 자동으로 기본 카드로 설정됩니다.
                    - 중복 카드 등록은 불가능합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 카드 정보",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @PostMapping
    public BaseResponse<PaymentMethodResponse> registerPaymentMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentMethodRequest request
    ) {
        String email = userDetails.getUsername();
        log.debug("Payment method registration request - email: {}", email);

        PaymentMethod paymentMethod = paymentMethodService.registerPaymentMethod(email, request);
        return BaseResponse.success(convertToResponse(paymentMethod));
    }

    @Operation(
            summary = "결제 수단 목록 조회",
            description = "사용자의 등록된 결제 수단 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @GetMapping
    public BaseResponse<List<PaymentMethodResponse>> getPaymentMethods(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        log.debug("Payment methods retrieval request - email: {}", email);

        List<PaymentMethodResponse> responses = paymentMethodService.getUserPaymentMethods(email);
        return BaseResponse.success(responses);
    }

    @Operation(
            summary = "결제 수단 상태 변경",
            description = """
                    결제 수단의 상태를 변경합니다.
                    - 만료된 카드는 상태 변경이 불가능합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))
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
                    responseCode = "404",
                    description = "결제 수단을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))
            )
    })
    @Parameter(name = "paymentMethodId", description = "Payment method ID", required = true)
    @PatchMapping("/{paymentMethodId}/status")
    public BaseResponse<PaymentMethodResponse> updatePaymentMethodStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("paymentMethodId")  Long paymentMethodId,
            @RequestParam PaymentMethodStatus status
    ) {
        String email = userDetails.getUsername();
        log.debug("Payment method status update request - email: {}, id: {}, status: {}",
                email, paymentMethodId, status);

        PaymentMethod updatedMethod = paymentMethodService.updatePaymentMethodStatus(
                email, paymentMethodId, status);
        return BaseResponse.success(convertToResponse(updatedMethod));
    }

    private PaymentMethodResponse convertToResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .maskedCardNumber(CardUtils.maskCardNumber(paymentMethod.getCardNumber()))
                .cardCompany(paymentMethod.getCardCompany())
                .cardHolderName(paymentMethod.getCardHolderName())
                .expiryMonth(paymentMethod.getExpiryMonth())
                .expiryYear(paymentMethod.getExpiryYear())
                .isDefault(paymentMethod.isDefault())
                .status(paymentMethod.getStatus())
                .alias(paymentMethod.getAlias())
                .build();
    }
}