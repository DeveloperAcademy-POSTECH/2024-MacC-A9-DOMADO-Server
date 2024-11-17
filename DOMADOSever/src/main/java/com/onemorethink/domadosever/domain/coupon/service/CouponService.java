package com.onemorethink.domadosever.domain.coupon.service;


import com.onemorethink.domadosever.domain.coupon.dto.CouponResponse;
import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.coupon.entity.CouponStatus;
import com.onemorethink.domadosever.domain.coupon.repository.CouponRepository;
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
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public List<CouponResponse> getUserCoupons(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Coupon> coupons = couponRepository.findByUserOrderByCreatedAtDesc(user);

        return coupons.stream()
                .map(this::convertToCouponResponse)
                .collect(Collectors.toList());
    }

    public List<CouponResponse> getAvailableCoupons(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Coupon> availableCoupons = couponRepository.findByUserAndStatusAndExpireDateAfter(
                user,
                CouponStatus.ACTIVE,
                LocalDateTime.now()
        );

        return availableCoupons.stream()
                .map(this::convertToCouponResponse)
                .collect(Collectors.toList());
    }

    private CouponResponse convertToCouponResponse(Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getId())
                .discountMinutes(coupon.getDiscountMinutes())
                .status(coupon.getStatus())
                .expireDate(coupon.getExpireDate())
                .usedAt(coupon.getUsedAt())
                .usedPaymentId(coupon.getUsedPayment() != null ?
                        coupon.getUsedPayment().getId() : null)
                .build();
    }
}