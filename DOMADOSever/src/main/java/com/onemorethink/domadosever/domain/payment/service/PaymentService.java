package com.onemorethink.domadosever.domain.payment.service;

import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.coupon.entity.CouponStatus;
import com.onemorethink.domadosever.domain.coupon.repository.CouponRepository;
import com.onemorethink.domadosever.domain.payment.entity.Payment;
import com.onemorethink.domadosever.domain.payment.entity.PaymentStatus;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethod;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethodStatus;
import com.onemorethink.domadosever.domain.payment.repository.PaymentMethodRepository;
import com.onemorethink.domadosever.domain.payment.repository.PaymentRepository;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CouponRepository couponRepository;

    private static final int UNLOCK_FEE = 100;  // 잠금해제 요금
    private static final int DAY_RATE = 30;     // 주간 요금(분당)
    private static final int NIGHT_RATE = 4;    // 야간 요금(분당)
    private static final LocalTime DAY_START = LocalTime.of(9, 0);    // 주간 시작
    private static final LocalTime DAY_END = LocalTime.of(18, 0);     // 주간 종료

    public Payment processRentalPayment(Rental rental, Long couponId) {
        // 1. 유효한 결제 수단 조회
        PaymentMethod paymentMethod = paymentMethodRepository
                .findFirstByUserAndStatusOrderByIdDesc(rental.getUser(), PaymentMethodStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_PAYMENT_METHOD));

        // 2. 쿠폰 조회 및 검증 (쿠폰ID가 제공된 경우에만)
        Coupon selectedCoupon = null;
        if (couponId != null) {
            selectedCoupon = couponRepository.findByIdAndUserAndStatusAndExpireDateAfter(
                    couponId,
                    rental.getUser(),
                    CouponStatus.ACTIVE,
                    LocalDateTime.now()
            ).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_COUPON));
        }

        // 3. 이용 요금 계산
        PaymentCalculationResult calculationResult = calculateTotalAmountWithCoupon(rental, selectedCoupon);

        // 4. 결제 요청 생성
        Payment payment = createPayment(rental, paymentMethod, calculationResult);

        try {
            // 5. 실제 PG사 결제 요청 (할인 후 금액이 100원 이상인 경우에만)
            if (calculationResult.getFinalAmount() >= UNLOCK_FEE) {
                processExternalPayment(payment);
            }

            // 6. 결제 성공 처리
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(generateTransactionId());

            // 7. 사용된 쿠폰 처리
            if (selectedCoupon != null) {
                applyCoupon(selectedCoupon, payment);
            }

        } catch (Exception e) {
            // 8. 결제 실패 처리
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            log.error("Payment failed for rental {}: {}", rental.getId(), e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_PROCESSING_FAILED);
        }

        return paymentRepository.save(payment);
    }

    private PaymentCalculationResult calculateTotalAmountWithCoupon(Rental rental, Coupon coupon) {
        // 1. 기본 요금 계산
        int originalAmount = calculateBaseAmount(rental);

        // 2. 쿠폰 할인 계산 (쿠폰이 제공된 경우에만)
        int discountAmount = 0;
        if (coupon != null) {
            discountAmount = calculateDiscountAmount(rental, coupon);
        }

        int finalAmount = Math.max(0, originalAmount - discountAmount);

        return PaymentCalculationResult.builder()
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .usedCoupon(coupon)
                .build();
    }

    private int calculateBaseAmount(Rental rental) {
        // 1. 기본 잠금해제 요금
        int totalAmount = UNLOCK_FEE;

        // 2. 이용 시간에 따른 요금 계산
        LocalDateTime startTime = rental.getStartTime();
        LocalDateTime endTime = rental.getEndTime() != null ?
                rental.getEndTime() : LocalDateTime.now();

        // 시간대별 요금 계산을 위해 1분 단위로 순회
        LocalDateTime current = startTime;
        while (current.isBefore(endTime)) {
            LocalTime currentTime = current.toLocalTime();

            // 주간/야간 요금 적용
            if (isDayTime(currentTime)) {
                totalAmount += DAY_RATE;
            } else {
                totalAmount += NIGHT_RATE;
            }

            current = current.plusMinutes(1);
        }

        return totalAmount;
    }

    private int calculateDiscountAmount(Rental rental, Coupon coupon) {
        // 쿠폰의 할인 시간(분)을 현재 시간대의 요금으로 계산
        int discountMinutes = Math.min(coupon.getDiscountMinutes(), rental.getUsageMinutes());
        LocalTime currentTime = LocalTime.now();
        int ratePerMinute = isDayTime(currentTime) ? DAY_RATE : NIGHT_RATE;
        return discountMinutes * ratePerMinute;
    }

    private void applyCoupon(Coupon coupon, Payment payment) {
        coupon.setUsedAt(LocalDateTime.now());
        coupon.setUsedPayment(payment);
        coupon.setStatus(CouponStatus.USED);
    }

    private boolean isDayTime(LocalTime time) {
        return !time.isBefore(DAY_START) && time.isBefore(DAY_END);
    }

    private Payment createPayment(Rental rental, PaymentMethod paymentMethod,
                                  PaymentCalculationResult calculationResult) {
        return Payment.builder()
                .user(rental.getUser())
                .rental(rental)
                .paymentMethod(paymentMethod)
                .amount(calculationResult.getFinalAmount())
                .originalAmount(calculationResult.getOriginalAmount())
                .discountAmount(calculationResult.getDiscountAmount())
                .usedCoupon(calculationResult.getUsedCoupon())
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Getter
    @Builder
    private static class PaymentCalculationResult {
        private final int originalAmount;    // 원래 금액
        private final int discountAmount;    // 할인 금액
        private final int finalAmount;       // 최종 결제 금액
        private final Coupon usedCoupon;     // 사용된 쿠폰
    }

    private int calculateTotalMinutes(Rental rental) {
        LocalDateTime endTime = rental.getEndTime() != null ?
                rental.getEndTime() : LocalDateTime.now();
        return (int) Duration.between(rental.getStartTime(), endTime).toMinutes();
    }

    private void processExternalPayment(Payment payment) {
        // TODO: 실제 PG사 결제 연동 구현
    }

    private String generateTransactionId() {
        // 거래 ID 생성 로직 (예: UUID + 타임스탬프)
        return "TR" + UUID.randomUUID().toString().substring(0, 8) +
                System.currentTimeMillis();
    }
}

