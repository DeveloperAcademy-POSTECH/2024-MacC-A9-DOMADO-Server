package com.onemorethink.domadosever.domain.payment.service;

import com.onemorethink.domadosever.domain.payment.dto.PaymentMethodRequest;
import com.onemorethink.domadosever.domain.payment.dto.PaymentMethodResponse;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethod;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethodStatus;
import com.onemorethink.domadosever.domain.payment.repository.PaymentMethodRepository;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.repository.UserRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import com.onemorethink.domadosever.global.error.exception.PaymentValidationException;
import com.onemorethink.domadosever.global.util.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentValidator paymentValidator;
    private final UserRepository userRepository;

    /**
     * 결제 수단 등록
     */
    @Transactional
    public PaymentMethod registerPaymentMethod(String email, PaymentMethodRequest request) {
        // 1. 요청을 보낸 사용자가 존재하는지 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 현재 사용자가 가지고 있는 카드 목록 확인
        List<PaymentMethod> existingMethods =
                paymentMethodRepository.findByUser_Id(user.getId());

        // 카드 정보 검증
        PaymentValidator.CardValidationResult validationResult =
                paymentValidator.validateCardRegistration(
                        request.getCardNumber(),
                        request.getCvv(),
                        request.getExpiryMonth(),
                        request.getExpiryYear(),
                        request.getCardHolderName(),
                        existingMethods
                );

        if (!validationResult.isValid()) {
            throw new PaymentValidationException(
                    ErrorCode.INVALID_CARD_NUMBER,
                    request.getCardNumber(),
                    validationResult.getErrors().get(0)
            );
        }

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setUser(user);
        paymentMethod.setCardNumber(request.getCardNumber());
        paymentMethod.setCardCompany(validationResult.getCardCompany());
        paymentMethod.setCardHolderName(request.getCardHolderName());
        paymentMethod.setExpiryMonth(request.getExpiryMonth());
        paymentMethod.setExpiryYear(request.getExpiryYear());

        // 첫 번째 등록 카드는 기본 카드로 설정
        if (existingMethods.isEmpty()) {
            paymentMethod.setDefault(true);
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * 사용자의 결제 수단 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getUserPaymentMethods(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<PaymentMethod> paymentMethods =
                paymentMethodRepository.findByUser_Id(user.getId());

        return paymentMethods.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 결제 수단 상태 변경
     */
    @Transactional
    public PaymentMethod updatePaymentMethodStatus(
            String userEmail,
            Long paymentMethodId,
            PaymentMethodStatus status) {

        PaymentMethod paymentMethod = paymentMethodRepository
                .findByIdAndUser_Email(paymentMethodId, userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        // 만료된 카드는 상태 변경 불가
        if (paymentMethod.getStatus() == PaymentMethodStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.INACTIVE_PAYMENT_METHOD);
        }

        paymentMethod.setStatus(status);
        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * 결제 수단 유효성 검증
     */
    public void validateCardInfo(PaymentMethod paymentMethod) {
        // 상태 검증
        if (paymentMethod.getStatus() != PaymentMethodStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_PAYMENT_METHOD);
        }

        // 만료 여부 검증
        if (isCardExpired(paymentMethod)) {
            paymentMethod.setStatus(PaymentMethodStatus.EXPIRED);
            paymentMethodRepository.save(paymentMethod);
            throw new BusinessException(ErrorCode.EXPIRED_CARD);
        }
    }

    /**
     * 카드 만료 여부 검증
     */
    private boolean isCardExpired(PaymentMethod paymentMethod) {
        try {
            int expiryMonth = Integer.parseInt(paymentMethod.getExpiryMonth());
            int expiryYear = Integer.parseInt(paymentMethod.getExpiryYear());

            LocalDate now = LocalDate.now();
            LocalDate expiryDate = LocalDate.of(2000 + expiryYear, expiryMonth, 1)
                    .plusMonths(1).minusDays(1);

            return expiryDate.isBefore(now);
        } catch (NumberFormatException | DateTimeException e) {
            log.error("Invalid expiry date format: month={}, year={}",
                    paymentMethod.getExpiryMonth(),
                    paymentMethod.getExpiryYear());
            return true;
        }
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