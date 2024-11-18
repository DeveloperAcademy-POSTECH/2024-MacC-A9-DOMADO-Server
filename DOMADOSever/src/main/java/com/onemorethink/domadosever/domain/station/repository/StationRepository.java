package com.onemorethink.domadosever.domain.station.repository;

import com.onemorethink.domadosever.domain.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
}
