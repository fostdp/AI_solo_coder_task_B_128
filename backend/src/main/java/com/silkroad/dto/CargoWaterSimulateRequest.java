package com.silkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoWaterSimulateRequest {

    private String cargoType;

    private Integer camelCount;

    private Integer crewCount;

    private Double cargoWeightKg;

    private Integer days;

    private String terrainType;

    private Double temperatureC;

    private String camelType;
}
