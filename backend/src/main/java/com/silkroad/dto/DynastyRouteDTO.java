package com.silkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynastyRouteDTO {

    private Long id;

    private String dynasty;

    private String dynastyName;

    private String name;

    private String nameEn;

    private Integer startYear;

    private Integer endYear;

    private Double totalDistanceKm;

    private String mainCommodities;

    private String description;

    private Double politicalStability;

    private Double tradeVolumeScore;

    private Double culturalExchangeScore;

    private Double evidenceStrength;

    private String historicalSources;

    private String archaeologicalNote;

    private String routeQuality;

    private Integer numArchaeologicalSites;

    private List<double[]> coordinates;
}
