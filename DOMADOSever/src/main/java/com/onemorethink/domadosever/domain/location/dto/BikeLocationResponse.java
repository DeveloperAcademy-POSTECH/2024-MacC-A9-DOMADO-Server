package com.onemorethink.domadosever.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BikeLocationResponse {
    private List<HubLocationDto> hubs;
    private List<HiBikeLocationDto> hiBikes;
}
