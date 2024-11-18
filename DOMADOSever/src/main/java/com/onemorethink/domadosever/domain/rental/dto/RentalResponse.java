package com.onemorethink.domadosever.domain.rental.dto;

import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RentalResponse {
    private Long rentalId;
    private Long bikeId;
    private LocalDateTime startTime;
    private String message;
    private BikeStatus bikeStatus;           // 자전거 상태 (PARKED, IN_USE 등)
    private HiBikeStatus hiBikeStatus;
}