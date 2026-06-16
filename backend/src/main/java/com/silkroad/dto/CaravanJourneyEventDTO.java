package com.silkroad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaravanJourneyEventDTO {

    private Long id;

    private Long virtualCaravanId;

    private String eventType;

    private String severity;

    private String title;

    private String message;

    private Double lng;

    private Double lat;

    private Double effectWaterLiters;

    private Double effectFoodDays;

    private Double effectMorale;

    private Integer effectGoldCoins;

    private Boolean isResolved;

    private LocalDateTime eventTime;
}
