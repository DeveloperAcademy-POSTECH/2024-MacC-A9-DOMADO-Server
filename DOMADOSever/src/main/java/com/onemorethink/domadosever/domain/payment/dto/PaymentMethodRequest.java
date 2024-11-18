package com.onemorethink.domadosever.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결제 수단 등록 요청")
public class PaymentMethodRequest {
    @Schema(
            description = "카드 번호",
            example = "5218260000000023", // 신한카드 JCB
            pattern = "^[0-9]{16}$",
            required = true
    )
    @NotBlank(message = "카드 번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{16}$", message = "카드 번호는 16자리 숫자여야 합니다")
    private String cardNumber;

    @Schema(
            description = "카드 회사 (신한카드, KB국민카드, 삼성카드, NH농협카드 등)",
            example = "신한카드",
            required = true
    )
    @NotBlank(message = "카드 회사는 필수입니다")
    private String cardCompany;

    @Schema(
            description = "카드 소유자 이름 (개인: 홍길동, 법인: (주)우리회사)",
            example = "홍길동",
            required = true
    )
    @NotBlank(message = "카드 소유자 이름은 필수입니다")
    private String cardHolderName;

    @Schema(
            description = "카드 유효기간 월 (01-12)",
            example = "12",
            pattern = "^(0[1-9]|1[0-2])$",
            required = true
    )
    @NotBlank(message = "유효기간(월)은 필수입니다")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "유효기간(월)은 01-12 사이여야 합니다")
    private String expiryMonth;

    @Schema(
            description = "카드 유효기간 년도 (24-29)",
            example = "25",
            pattern = "^[0-9]{2}$",
            required = true
    )
    @NotBlank(message = "유효기간(년)은 필수입니다")
    @Pattern(regexp = "^[0-9]{2}$", message = "유효기간(년)은 2자리 숫자여야 합니다")
    private String expiryYear;

    @Schema(
            description = "카드 보안코드(CVV) - 일반:3자리, AMEX:4자리",
            example = "123",
            pattern = "^[0-9]{3,4}$",
            required = true
    )
    @NotBlank(message = "보안코드는 필수입니다")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "보안코드는 3-4자리 숫자여야 합니다")
    private String cvv;

    @Schema(
            description = "기본 결제수단 여부",
            example = "false",
            defaultValue = "false"
    )
    private boolean isDefault;
}