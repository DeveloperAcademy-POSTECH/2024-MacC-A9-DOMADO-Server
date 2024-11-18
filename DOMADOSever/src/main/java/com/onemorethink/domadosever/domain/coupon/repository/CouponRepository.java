package com.onemorethink.domadosever.domain.coupon.repository;

import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.coupon.entity.CouponStatus;
import com.onemorethink.domadosever.domain.payment.entity.Payment;
import com.onemorethink.domadosever.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 사용자의 전체 쿠폰 조회 (최신순)
    List<Coupon> findByUserOrderByCreatedAtDesc(User user);

    // 사용 가능한 쿠폰 조회 (만료되지 않고 사용되지 않은 쿠폰)
    @Query("SELECT c FROM Coupon c WHERE c.user = :user " +
            "AND c.status = :status " +
            "AND c.expireDate > :now")
    List<Coupon> findByUserAndStatusAndExpireDateAfter(
            @Param("user") User user,
            @Param("status") CouponStatus status,
            @Param("now") LocalDateTime now
    );

    // 특정 기간 내 만료 예정인 쿠폰 조회 (알림 발송용)
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
            "AND c.expireDate BETWEEN :start AND :end")
    List<Coupon> findExpiringCoupons(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 특정 사용자의 사용 가능한 쿠폰 수 조회
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.user = :user " +
            "AND c.status = 'ACTIVE' " +
            "AND c.expireDate > CURRENT_TIMESTAMP")
    long countAvailableCoupons(@Param("user") User user);

    // 특정 결제에 사용된 쿠폰 조회
    Optional<Coupon> findByUsedPayment(Payment payment);

    // 만료된 쿠폰 상태 일괄 업데이트
    @Modifying
    @Query("UPDATE Coupon c SET c.status = 'EXPIRED' " +
            "WHERE c.status = 'ACTIVE' AND c.expireDate <= :now")
    void updateExpiredCoupons(@Param("now") LocalDateTime now);

    // 특정 사용자의 유효한 쿠폰 조회
    Optional<Coupon> findByIdAndUserAndStatusAndExpireDateAfter(
            Long id,
            User user,
            CouponStatus status,
            LocalDateTime expireDate
    );

    // 사용자의 가장 오래된(먼저 발급된) 유효한 쿠폰 조회
    @Query("SELECT c FROM Coupon c " +
            "WHERE c.user = :user " +
            "AND c.status = :status " +
            "AND c.expireDate > :currentTime " +
            "ORDER BY c.createdAt ASC")  // DESC에서 ASC로 변경
    Optional<Coupon> findFirstByUserAndStatusAndExpireDateAfter(
            @Param("user") User user,
            @Param("status") CouponStatus status,
            @Param("currentTime") LocalDateTime currentTime
    );

    // 사용자의 모든 유효한 쿠폰 조회 (만료일이 빠른 순)
    @Query("SELECT c FROM Coupon c " +
            "WHERE c.user = :user " +
            "AND c.status = :status " +
            "AND c.expireDate > :currentTime " +
            "ORDER BY c.createdAt ASC, c.expireDate ASC")  // 발급일, 만료일 순으로 정렬
    List<Coupon> findAllValidCoupons(
            @Param("user") User user,
            @Param("status") CouponStatus status,
            @Param("currentTime") LocalDateTime currentTime
    );

}