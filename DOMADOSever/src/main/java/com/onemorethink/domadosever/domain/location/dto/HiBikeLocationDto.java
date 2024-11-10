package com.onemorethink.domadosever.domain.location.dto;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HiBikeLocationDto {
    private Long bikeId;
    private String qrCode;
    private Double latitude;
    private Double longitude;
    private Integer batteryLevel;
    private BikeStatus status;
    private HiBikeStatus hiBikeStatus;
    private Long homeHubId;
    private String homeHubName;

    public static HiBikeLocationDto from(Bike bike) {
        return HiBikeLocationDto.builder()
                .bikeId(bike.getId())
                .qrCode(bike.getQrCode())
                .latitude(bike.getCurrentLatitude())
                .longitude(bike.getCurrentLongitude())
                .batteryLevel(bike.getBatteryLevel())
                .status(bike.getStatus())
                .hiBikeStatus(bike.getHiBikeStatus())
                .homeHubId(bike.getHomeHub().getId())
                .homeHubName(bike.getHomeHub().getName())
                .build();
    }
}