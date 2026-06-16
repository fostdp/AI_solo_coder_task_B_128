package com.silkroad.vr_caravan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualCaravanDTO {

    private Long id;

    private String sessionId;

    private String name;

    private String leaderName;

    private Long routeId;

    private Double lng;

    private Double lat;

    private Double progressPct;

    private String status;

    private Double speedKmh;

    private String cargoType;

    private Double cargoWeightKg;

    private Integer camelCount;

    private Integer crewCount;

    private Double waterSupplyLiters;

    private Double waterCapacityLiters;

    private Double foodSupplyDays;

    private Double morale;

    private Integer goldCoins;

    private Double distanceTraveledKm;

    private Integer journeyDaysElapsed;

    private String season;

    private Boolean isPublic;

    private LocalDateTime startedAt;

    private LocalDateTime lastActiveAt;
}
