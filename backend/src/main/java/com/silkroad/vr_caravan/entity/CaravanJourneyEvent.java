package com.silkroad.vr_caravan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "caravan_journey_events")
public class CaravanJourneyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "virtual_caravan_id", nullable = false)
    private Long virtualCaravanId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    private String severity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String message;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    @Column(name = "effect_water_liters")
    private Double effectWaterLiters;

    @Column(name = "effect_food_days")
    private Double effectFoodDays;

    @Column(name = "effect_morale")
    private Double effectMorale;

    @Column(name = "effect_gold_coins")
    private Integer effectGoldCoins;

    @Column(name = "is_resolved")
    private Boolean isResolved;

    @Column(name = "event_time")
    private LocalDateTime eventTime;
}
