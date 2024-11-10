package com.onemorethink.domadosever.domain.bike.repository;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {
    // 기본 JPQL 쿼리 (H2용)
    @Query("""
        SELECT b FROM Bike b
        LEFT JOIN FETCH b.homeHub
        WHERE b.status = 'TEMPORARY_LOCKED'
        AND b.hiBikeStatus = 'AVAILABLE_FOR_RENT'
        AND b.batteryLevel >= :minBatteryLevel
        AND ABS(b.currentLatitude - :latitude) <= :radius / 111.0
        AND ABS(b.currentLongitude - :longitude) <= :radius / (111.0 * COS(RADIANS(:latitude)))
        """)
    List<Bike> findAvailableHiBikesWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radiusInKm,
            @Param("minBatteryLevel") Integer minBatteryLevel
    );
}