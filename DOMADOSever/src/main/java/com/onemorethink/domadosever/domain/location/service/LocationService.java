package com.onemorethink.domadosever.domain.location.service;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.bike.repository.BikeRepository;
import com.onemorethink.domadosever.domain.location.dto.BikeLocationResponse;
import com.onemorethink.domadosever.domain.location.dto.HiBikeLocationDto;
import com.onemorethink.domadosever.domain.location.dto.HubLocationDto;
import com.onemorethink.domadosever.domain.location.dto.StationLocationDto;
import com.onemorethink.domadosever.domain.station.entity.Hub;
import com.onemorethink.domadosever.domain.station.entity.Station;
import com.onemorethink.domadosever.domain.station.repository.HubRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocationService {
    private final HubRepository hubRepository;
    private final BikeRepository bikeRepository;

    private static final int MIN_BATTERY_LEVEL = 20;

    /**
     * 주어진 위치 반경 내의 대여 가능한 자전거 위치 정보를 조회합니다.
     */
    public BikeLocationResponse findBikeLocations(Double latitude, Double longitude, Double radius) {
        try {
            // 1. 허브와 연관된 스테이션, 자전거 정보 조회 및 변환
            List<Hub> nearbyHubs = hubRepository.findHubsWithinRadius(latitude, longitude, radius);
            List<HubLocationDto> hubLocations = convertToHubLocationDtos(nearbyHubs);

            // 2. HiBike 자전거 조회 및 변환
            List<Bike> availableHiBikes = bikeRepository.findAvailableHiBikesWithinRadius(
                    latitude, longitude, radius, MIN_BATTERY_LEVEL);
            List<HiBikeLocationDto> hiBikeLocations = availableHiBikes.stream()
                    .map(HiBikeLocationDto::from)
                    .collect(Collectors.toList());

            return BikeLocationResponse.builder()
                    .hubs(hubLocations)
                    .hiBikes(hiBikeLocations)
                    .build();

        } catch (Exception e) {
            log.error("Failed to retrieve bike locations. lat: {}, lon: {}, radius: {}km",
                    latitude, longitude, radius, e);
            throw new BusinessException(ErrorCode.LOCATION_SERVICE_ERROR);
        }
    }

    /**
     * 허브 목록을 HubLocationDto 목록으로 변환합니다.
     * 모든 허브 정보를 포함하되, 대여 가능한 자전거 수는 실제 대여 가능한 자전거만 계산합니다.
     */
    private List<HubLocationDto> convertToHubLocationDtos(List<Hub> hubs) {
        return hubs.stream()
                .map(hub -> {
                    List<StationLocationDto> stationDtos = convertToStationLocationDtos(hub.getStations());
                    return HubLocationDto.from(hub, stationDtos);
                })
                .collect(Collectors.toList());
    }

    /**
     * 스테이션 목록을 StationLocationDto 목록으로 변환합니다.
     * 모든 스테이션 정보를 포함하되, 대여 가능한 자전거 목록은 실제 대여 가능한 자전거만 포함합니다.
     */
    private List<StationLocationDto> convertToStationLocationDtos(List<Station> stations) {
        return stations.stream()
                .map(StationLocationDto::from)  // StationLocationDto.from() 메서드는 이미 대여 가능한 자전거만 필터링
                .collect(Collectors.toList());
    }
}
