package com.onemorethink.domadosever.domain.bike.repository;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {
    @Query("""
        SELECT b FROM Bike b
        LEFT JOIN FETCH b.homeHub
        WHERE b.status = 'TEMPORARY_LOCKED'
        AND b.hiBikeStatus = 'AVAILABLE_FOR_RENT'
        AND b.batteryLevel >= :minBatteryLevel
        AND ABS(CAST(b.currentLatitude AS double) - CAST(:latitude AS double)) <= (CAST(:radius AS double) / 111.0)
        AND ABS(CAST(b.currentLongitude AS double) - CAST(:longitude AS double)) <= (CAST(:radius AS double) / (111.0 * COS(RADIANS(CAST(:latitude AS double)))))
        """)
    List<Bike> findAvailableHiBikesWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radiusInKm,
            @Param("minBatteryLevel") Integer minBatteryLevel
    );

    Optional<Bike> findByQrCode(String qrCode);
}