package com.onemorethink.domadosever.domain.coupon.repository;

import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.coupon.entity.Stamp;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {

    // 사용자의 미사용 스탬프 개수 조회
    long countByUserAndIsUsedFalse(User user);

    // 사용자의 미사용 스탬프 N개 조회 (쿠폰 교환용)
    @Query("SELECT s FROM Stamp s WHERE s.user = :user AND s.isUsed = false " +
            "ORDER BY s.createdAt ASC LIMIT :limit")
    List<Stamp> findTopNByUserAndIsUsedFalse(
            @Param("user") User user,
            @Param("limit") int limit
    );

    // 사용자의 전체 스탬프 조회 (최신순)
    List<Stamp> findByUserOrderByCreatedAtDesc(User user);

    // 특정 쿠폰으로 교환된 스탬프 조회
    List<Stamp> findByExchangedCoupon(Coupon coupon);

    // 특정 기간 내 획득한 스탬프 조회
    @Query("SELECT s FROM Stamp s WHERE s.user = :user " +
            "AND s.createdAt BETWEEN :startDate AND :endDate")
    List<Stamp> findByUserAndCreatedAtBetween(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 특정 대여건으로 발급된 스탬프 조회
    Optional<Stamp> findByRental(Rental rental);
}