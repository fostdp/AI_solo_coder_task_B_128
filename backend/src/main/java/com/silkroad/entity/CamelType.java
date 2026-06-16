package com.silkroad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "camel_types")
public class CamelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", nullable = false, unique = true)
    private String typeCode;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "type_name_en")
    private String typeNameEn;

    @Column(name = "avg_body_weight_kg")
    private Double avgBodyWeightKg;

    @Column(name = "body_height_m")
    private Double bodyHeightM;

    @Column(name = "optimal_load_ratio")
    private Double optimalLoadRatio;

    @Column(name = "max_load_ratio")
    private Double maxLoadRatio;

    @Column(name = "base_water_per_kg_body")
    private Double baseWaterPerKgBody;

    @Column(name = "water_temp_coefficient")
    private Double waterTempCoefficient;

    @Column(name = "base_speed_kmh")
    private Double baseSpeedKmh;

    @Column(name = "load_speed_decay_factor")
    private Double loadSpeedDecayFactor;

    @Column(name = "heat_resistance_score")
    private Double heatResistanceScore;

    @Column(name = "cold_resistance_score")
    private Double coldResistanceScore;

    @Column(name = "stamina_score")
    private Double staminaScore;

    @Column(name = "daily_distance_km")
    private Double dailyDistanceKm;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "origin_region")
    private String originRegion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
