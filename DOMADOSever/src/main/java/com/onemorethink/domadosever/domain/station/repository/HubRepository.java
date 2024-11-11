package com.onemorethink.domadosever.domain.station.repository;


import com.onemorethink.domadosever.domain.station.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HubRepository extends JpaRepository<Hub, Long> {
    @Query("""
    SELECT DISTINCT h FROM Hub h
    JOIN FETCH h.stations s
    LEFT JOIN FETCH s.parkedBikes b
    LEFT JOIN FETCH b.homeHub
    WHERE ABS(CAST(s.latitude AS double) - CAST(:latitude AS double)) <= (CAST(:radius AS double) / 111.0)
    AND ABS(CAST(s.longitude AS double) - CAST(:longitude AS double)) <= (CAST(:radius AS double) / (111.0 * COS(RADIANS(CAST(:latitude AS double)))))
    """)
    List<Hub> findHubsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radiusInKm
    );
}