package com.onemorethink.domadosever.domain.station.repository;


import com.onemorethink.domadosever.domain.station.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HubRepository extends JpaRepository<Hub, Long> {
    // 기본 JPQL 쿼리 (H2용)
    @Query("""
        SELECT DISTINCT h FROM Hub h
        JOIN FETCH h.stations s
        LEFT JOIN FETCH s.parkedBikes b
        LEFT JOIN FETCH b.homeHub
        WHERE ABS(s.latitude - :latitude) <= :radius / 111.0
        AND ABS(s.longitude - :longitude) <= :radius / (111.0 * COS(RADIANS(:latitude)))
        """)
    List<Hub> findHubsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radiusInKm
    );
}