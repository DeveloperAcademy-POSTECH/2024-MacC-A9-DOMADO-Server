package com.onemorethink.domadosever.domain.rental.dto;

import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RentalReturnResponse {
    private Long rentalId;
    private Long bikeId;
    private LocalDateTime endTime;
    private Integer usageMinutes;
    private Integer pauseMinutes;
    private Integer paymentAmount;
    private BikeStatus bikeStatus;
    private HiBikeStatus hiBikeStatus;
    private Long stationId;
    private StampIssuanceInfo stampInfo;
    private String message;
}