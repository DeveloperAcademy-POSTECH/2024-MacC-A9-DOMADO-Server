package com.onemorethink.domadosever.domain.location.dto;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import com.onemorethink.domadosever.domain.station.entity.Station;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class StationLocationDto {
    private Long stationId;
    private String stationName;
    private Double latitude;
    private Double longitude;
    private int capacity;
    private List<BikeInfo> availableBikes;

    @Getter
    @Builder
    public static class BikeInfo {
        private Long bikeId;
        private String qrCode;
        private Integer batteryLevel;
        private BikeStatus status;
        private HiBikeStatus hiBikeStatus;
        private Integer currentDockId;
        private Long homeHubId;
        private String homeHubName;
    }

    public static StationLocationDto from(Station station) {
        List<BikeInfo> bikeInfos = station.getParkedBikes().stream()
                .filter(bike -> bike.getStatus() == BikeStatus.PARKED
                        && bike.getHiBikeStatus() == HiBikeStatus.NONE
                        && bike.getBatteryLevel() >= 20) // DTO 에서 담을 때도 배터리 상태 검증 로직
                .sorted(Comparator.comparing(
                        Bike::getCurrentDockId,
                        Comparator.nullsLast(Comparator.naturalOrder())

                        )
                )
                .map(bike -> BikeInfo.builder()
                        .bikeId(bike.getId())
                        .qrCode(bike.getQrCode())
                        .batteryLevel(bike.getBatteryLevel())
                        .status(bike.getStatus())
                        .hiBikeStatus(bike.getHiBikeStatus())
                        .currentDockId(bike.getCurrentDockId())
                        .homeHubId(bike.getHomeHub().getId())
                        .homeHubName(bike.getHomeHub().getName())
                        .build())
                .collect(Collectors.toList());

        return StationLocationDto.builder()
                .stationId(station.getId())
                .stationName(station.getName())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .capacity(station.getCapacity())
                .availableBikes(bikeInfos)
                .build();
    }
}