package com.onemorethink.domadosever.domain.location.dto;

import com.onemorethink.domadosever.domain.station.entity.Hub;
import com.onemorethink.domadosever.domain.station.entity.Station;
import lombok.Builder;
import lombok.Getter;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Getter
@Builder
public class HubLocationDto {
    private Long hubId;
    private String hubName;
    private Double latitude;  // 허브 내 스테이션들의 중심점 위도
    private Double longitude; // 허브 내 스테이션들의 중심점 경도
    private int totalAvailableBikes;  // 허브 내 전체 이용 가능한 자전거 수
    private List<StationLocationDto> stations;

    public static HubLocationDto from(Hub hub, List<StationLocationDto> stationDtos) {
        // 허브 내 모든 스테이션의 위치 중심점 계산
        DoubleSummaryStatistics latStats = hub.getStations().stream()
                .mapToDouble(Station::getLatitude)
                .summaryStatistics();
        DoubleSummaryStatistics lngStats = hub.getStations().stream()
                .mapToDouble(Station::getLongitude)
                .summaryStatistics();

        Double centerLat = (latStats.getMin() + latStats.getMax()) / 2;
        Double centerLng = (lngStats.getMin() + lngStats.getMax()) / 2;

        int totalBikes = stationDtos.stream()
                .mapToInt(station -> station.getAvailableBikes().size())
                .sum();

        return HubLocationDto.builder()
                .hubId(hub.getId())
                .hubName(hub.getName())
                .latitude(centerLat)
                .longitude(centerLng)
                .totalAvailableBikes(totalBikes)
                .stations(stationDtos)
                .build();
    }
}