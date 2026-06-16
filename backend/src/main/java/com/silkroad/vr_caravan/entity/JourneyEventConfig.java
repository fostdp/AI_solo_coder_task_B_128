package com.silkroad.vr_caravan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journey_event_configs")
public class JourneyEventConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_name")
    private String eventName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "terrain_types")
    private String terrainTypes;

    @Column(name = "min_occurrence_prob")
    private Double minOccurrenceProb;

    @Column(name = "max_occurrence_prob")
    private Double maxOccurrenceProb;

    private String severity;

    @Column(name = "water_effect_min")
    private Double waterEffectMin;

    @Column(name = "water_effect_max")
    private Double waterEffectMax;

    @Column(name = "food_effect_min")
    private Double foodEffectMin;

    @Column(name = "food_effect_max")
    private Double foodEffectMax;

    @Column(name = "morale_effect_min")
    private Double moraleEffectMin;

    @Column(name = "morale_effect_max")
    private Double moraleEffectMax;

    @Column(name = "gold_effect_min")
    private Integer goldEffectMin;

    @Column(name = "gold_effect_max")
    private Integer goldEffectMax;

    @Column(name = "is_positive")
    private Boolean isPositive;
}
