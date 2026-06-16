package com.silkroad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "modern_roads")
public class ModernRoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "road_type")
    private String roadType;

    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString geom;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "speed_limit_kmh")
    private Double speedLimitKmh;

    @Column(name = "lane_count")
    private Integer laneCount;

    private Boolean paved;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "corresponding_ancient_route_id")
    private Long correspondingAncientRouteId;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
