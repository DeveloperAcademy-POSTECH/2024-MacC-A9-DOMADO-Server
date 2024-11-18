package com.onemorethink.domadosever.domain.coupon.service;

import com.onemorethink.domadosever.domain.coupon.dto.StampResponse;
import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.coupon.entity.CouponStatus;
import com.onemorethink.domadosever.domain.coupon.entity.Stamp;
import com.onemorethink.domadosever.domain.coupon.repository.CouponRepository;
import com.onemorethink.domadosever.domain.coupon.repository.StampRepository;
import com.onemorethink.domadosever.domain.rental.dto.StampIssuanceInfo;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.repository.UserRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StampService {
    private final StampRepository stampRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    // private final NotificationService notificationService;

    private static final int STAMPS_NEEDED_FOR_COUPON = 5;
    private static final int COUPON_DISCOUNT_MINUTES = 30;  // 30분 무료 쿠폰
    private static final int COUPON_VALIDITY_DAYS = 30;     // 쿠폰 유효기간 30일

    public Stamp createStamp(User user, Rental rental) {
        // 1. 스탬프 생성
        Stamp stamp = Stamp.builder()
                .user(user)
                .rental(rental)
                .isUsed(false)
                .build();

        stampRepository.save(stamp);
        log.info("Stamp created for user: {}, rental: {}", user.getId(), rental.getId());

        // 2. 스탬프 개수 확인 및 쿠폰 발급 처리
        checkAndIssueCoupon(user);

        return stamp;
    }

    private void checkAndIssueCoupon(User user) {
        // 1. 미사용 스탬프 개수 조회
        long unusedStampCount = stampRepository.countByUserAndIsUsedFalse(user);

        // 2. 스탬프가 5개 이상이면 쿠폰 발급
        if (unusedStampCount >= STAMPS_NEEDED_FOR_COUPON) {
            // 3. 스탬프 5개를 사용 처리
            List<Stamp> stamps = stampRepository.findTopNByUserAndIsUsedFalse(
                    user, STAMPS_NEEDED_FOR_COUPON);

            Coupon coupon = issueCoupon(user, stamps);

            // 4. 스탬프 사용 처리
            stamps.forEach(stamp -> {
                stamp.setUsed(true);
                stamp.setExchangedCoupon(coupon);
                stampRepository.save(stamp);
            });

            // 5. 쿠폰 발급 알림
            // sendCouponIssuanceNotification(user, coupon);
        }
    }

    private Coupon issueCoupon(User user, List<Stamp> stamps) {
        Coupon coupon = Coupon.builder()
                .user(user)
                .stamps(stamps)
                .discountMinutes(COUPON_DISCOUNT_MINUTES)
                .expireDate(LocalDateTime.now().plusDays(COUPON_VALIDITY_DAYS))
                .status(CouponStatus.ACTIVE)
                .build();

        return couponRepository.save(coupon);
    }

//    private void sendCouponIssuanceNotification(User user, Coupon coupon) {
//        NotificationRequest notification = NotificationRequest.builder()
//                .userId(user.getId())
//                .title("🎉 무료 이용 쿠폰이 발급되었습니다!")
//                .body(String.format(
//                        "HiBike 스탬프 5개 적립 보상으로 %d분 무료 이용권이 발급되었습니다.\n" +
//                                "유효기간: ~ %s",
//                        coupon.getDiscountMinutes(),
//                        coupon.getExpireDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//                ))
//                .data(Map.of(
//                        "type", "COUPON_ISSUED",
//                        "couponId", coupon.getId().toString()
//                ))
//                .build();
//
//        notificationService.sendPushNotification(notification);
//    }

    // 유저가 가진 스템프 조회
    public List<StampResponse> getUserStamps(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Stamp> stamps = stampRepository.findByUserOrderByCreatedAtDesc(user);

        return stamps.stream()
                .map(this::convertToStampResponse)
                .collect(Collectors.toList());
    }

    private StampResponse convertToStampResponse(Stamp stamp) {
        return StampResponse.builder()
                .stampId(stamp.getId())
                .isUsed(stamp.isUsed())
                .createdAt(stamp.getCreatedAt())
                .rentalId(stamp.getRental().getId())
                .exchangedCouponId(stamp.getExchangedCoupon() != null ?
                        stamp.getExchangedCoupon().getId() : null)
                .build();
    }

    /**
     * 사용자의 미사용 스탬프 개수 조회
     */
    public int countUnusedStamps(User user) {
        return (int) stampRepository.countByUserAndIsUsedFalse(user);
    }

    /**
     * 사용자의 가장 최근 발급된 쿠폰 ID 조회
     */
    public Long getLastIssuedCouponId(User user) {
        return couponRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .map(Coupon::getId)
                .orElse(null);
    }

    /**
     * HiBike 이용 후 스탬프 발급 처리
     * @return StampIssuanceInfo - 스탬프 발급 관련 정보
     */
    public StampIssuanceInfo issueStampForHiBikeUse(Rental rental) {
        // 1. 스탬프 발급
        Stamp stamp = createStamp(rental.getUser(), rental);

        // 2. 미사용 스탬프 개수 확인
        int unusedStamps = countUnusedStamps(rental.getUser());

        // 3. 쿠폰 발급 여부 확인 (5개 모았을 때)
        boolean couponIssued = unusedStamps % STAMPS_NEEDED_FOR_COUPON == 0;

        // 4. 발급된 쿠폰 ID 조회
        Long couponId = null;
        if (couponIssued) {
            couponId = getLastIssuedCouponId(rental.getUser());
        }

        // 5. 스탬프 발급 정보 반환
        return StampIssuanceInfo.builder()
                .isIssued(true)
                .stampId(stamp.getId())
                .totalUnusedStamps(unusedStamps)
                .couponIssued(couponIssued)
                .issuedCouponId(couponId)
                .build();
    }
}