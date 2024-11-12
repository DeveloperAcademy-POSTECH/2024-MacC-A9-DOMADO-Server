package com.onemorethink.domadosever.domain.payment.dto;

import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethodStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "결제 수단 응답")
public class PaymentMethodResponse {
    @Schema(description = "결제 수단 ID", example = "1")
    private Long id;

    @Schema(
            description = "마스킹된 카드 번호",
            example = "123456******1234"
    )
    private String maskedCardNumber;

    @Schema(
            description = "카드 회사",
            example = "신한카드"
    )
    private String cardCompany;

    @Schema(
            description = "카드 소유자 이름",
            example = "홍길동"
    )
    private String cardHolderName;

    @Schema(
            description = "카드 유효기간 월",
            example = "12"
    )
    private String expiryMonth;

    @Schema(
            description = "카드 유효기간 년도",
            example = "25"
    )
    private String expiryYear;

    @Schema(
            description = "기본 결제수단 여부",
            example = "true"
    )
    private boolean isDefault;

    @Schema(
            description = "결제 수단 상태",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "EXPIRED"}
    )
    private PaymentMethodStatus status;

    @Schema(
            description = "결제 수단 별칭",
            example = "나의 신한카드"
    )
    private String alias;

    @Schema(
            description = "등록일시",
            example = "2024-01-01T12:00:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "카드 브랜드",
            example = "VISA",
            allowableValues = {"VISA", "MASTERCARD", "AMEX", "JCB"}
    )
    private String cardBrand;

    @Schema(
            description = "카드 종류",
            example = "신용",
            allowableValues = {"신용", "체크", "기프트"}
    )
    private String cardType;
}
