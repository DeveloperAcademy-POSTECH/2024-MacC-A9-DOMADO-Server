package com.onemorethink.domadosever.domain.rental.dto;

import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HiBikeResponse {
    private Long rentalId;
    private Long bikeId;
    private BikeStatus bikeStatus;
    private HiBikeStatus hiBikeStatus;
    private String message;
}