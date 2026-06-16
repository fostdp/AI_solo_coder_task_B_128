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

    @Column(name = "road_number")
    private String roadNumber;

    @Column(name = "road_class")
    private String roadClass;

    @Column(name = "pavement_type")
    private String pavementType;

    @Column(name = "design_speed_kmh")
    private Integer designSpeedKmh;

    @Column(name = "lane_width_m")
    private Double laneWidthM;

    @Column(name = "admin_level")
    private String adminLevel;

    @Column(name = "total_length_km")
    private Double totalLengthKm;

    @Column(name = "opening_year")
    private Integer openingYear;

    @Column(name = "standard_name")
    private String standardName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
