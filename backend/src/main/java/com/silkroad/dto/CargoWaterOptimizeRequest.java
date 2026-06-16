package com.silkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoWaterOptimizeRequest {

    private String cargoType;

    private Integer camelCount;

    private Integer crewCount;

    private Double cargoWeightKg;

    private String terrainType;

    private Double temperatureC;
}
