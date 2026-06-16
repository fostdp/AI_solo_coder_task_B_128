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
public class ModernRoadDTO {

    private Long id;

    private String name;

    private String nameEn;

    private String roadType;

    private Double totalDistanceKm;

    private Double speedLimitKmh;

    private Integer laneCount;

    private Boolean paved;

    private Integer yearBuilt;

    private Long correspondingAncientRouteId;

    private String description;

    private List<double[]> coordinates;
}
