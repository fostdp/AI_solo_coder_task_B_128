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
public class DynastyComparisonDTO {

    private String dynasty;

    private String dynastyName;

    private Integer startYear;

    private Integer endYear;

    private Integer routeCount;

    private Double totalDistanceSum;

    private Double avgPoliticalStability;

    private Double avgTradeVolume;

    private Double avgCulturalExchange;

    private List<DynastyRouteDTO> routes;
}
