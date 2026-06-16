package com.silkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoWaterOptimizationDTO {

    private String cargoType;

    private String cargoName;

    private Integer camelCount;

    private Integer crewCount;

    private Double cargoWeightKg;

    private Double optimalCargoKg;

    private Double maxCargoKg;

    private Double dailyWaterConsumptionLiters;

    private Double dailyCargoWaterRatio;

    private Double waterDaysEstimate;

    private Double recommendedWaterCapacity;

    private String suggestion;

    private String terrainType;

    private Double temperatureC;
}
