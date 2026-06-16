package com.silkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVirtualCaravanRequest {

    private String sessionId;

    private String name;

    private String leaderName;

    private Long routeId;

    private String cargoType;

    private Double cargoWeightKg;

    private Integer camelCount;

    private Integer crewCount;

    private String season;

    private Boolean isPublic;
}
