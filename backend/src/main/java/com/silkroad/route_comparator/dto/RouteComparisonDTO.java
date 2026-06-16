package com.silkroad.route_comparator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteComparisonDTO {

    private String ancientRouteName;

    private String modernRoadName;

    private Double ancientDistanceKm;

    private Double modernDistanceKm;

    private Double distanceDiffKm;

    private Double ancientTravelDays;

    private Double modernTravelHours;

    private Double ancientWaterRequiredLiters;

    private Double timeSavedRatio;

    private Double overlapPct;

    private String analysis;
}
