package com.silkroad.entity;

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
@Table(name = "virtual_caravans")
public class VirtualCaravan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(nullable = false)
    private String name;

    @Column(name = "leader_name")
    private String leaderName;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "start_waypoint_id")
    private Long startWaypointId;

    @Column(name = "end_waypoint_id")
    private Long endWaypointId;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point currentPosition;

    @Column(name = "current_waypoint_id")
    private Long currentWaypointId;

    @Column(name = "progress_pct")
    private Double progressPct;

    private String status;

    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Column(name = "cargo_type")
    private String cargoType;

    @Column(name = "cargo_weight_kg")
    private Double cargoWeightKg;

    @Column(name = "camel_count")
    private Integer camelCount;

    @Column(name = "camel_type")
    private String camelType;

    @Column(name = "speed_multiplier")
    private Double speedMultiplier = 2.0;

    @Column(name = "simulation_speed_mode")
    private String simulationSpeedMode = "NORMAL";

    @Column(name = "crew_count")
    private Integer crewCount;

    @Column(name = "water_supply_liters")
    private Double waterSupplyLiters;

    @Column(name = "water_capacity_liters")
    private Double waterCapacityLiters;

    @Column(name = "food_supply_days")
    private Double foodSupplyDays;

    private Double morale;

    @Column(name = "gold_coins")
    private Integer goldCoins;

    @Column(name = "distance_traveled_km")
    private Double distanceTraveledKm;

    @Column(name = "journey_days_elapsed")
    private Integer journeyDaysElapsed;

    private String season;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
