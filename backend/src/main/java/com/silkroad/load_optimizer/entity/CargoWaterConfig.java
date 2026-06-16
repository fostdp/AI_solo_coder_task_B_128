package com.silkroad.load_optimizer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cargo_water_configs")
public class CargoWaterConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cargo_type", nullable = false)
    private String cargoType;

    @Column(name = "cargo_name")
    private String cargoName;

    @Column(name = "water_per_100kg_per_day_liters")
    private Double waterPer100kgPerDayLiters;

    @Column(name = "camel_base_water_daily_l")
    private Double camelBaseWaterDailyL;

    @Column(name = "crew_base_water_daily_l")
    private Double crewBaseWaterDailyL;

    @Column(name = "terrain_factor_desert")
    private Double terrainFactorDesert;

    @Column(name = "terrain_factor_mountains")
    private Double terrainFactorMountains;

    @Column(name = "terrain_factor_oasis")
    private Double terrainFactorOasis;

    @Column(name = "temperature_factor_per_degree")
    private Double temperatureFactorPerDegree;

    @Column(name = "max_cargo_per_camel_kg")
    private Double maxCargoPerCamelKg;

    @Column(name = "optimal_cargo_per_camel_kg")
    private Double optimalCargoPerCamelKg;

    @Column(name = "camel_type_code")
    private String camelTypeCode;

    @Column(name = "base_speed_kmh")
    private Double baseSpeedKmh;

    @Column(name = "load_speed_decay_factor")
    private Double loadSpeedDecayFactor;

    @Column(name = "daily_distance_km")
    private Double dailyDistanceKm;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
