package com.silkroad.route_evolution.entity;

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
@Table(name = "dynasty_routes")
public class DynastyRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dynasty;

    @Column(name = "dynasty_name")
    private String dynastyName;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString geom;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "main_commodities")
    private String mainCommodities;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "political_stability")
    private Double politicalStability;

    @Column(name = "trade_volume_score")
    private Double tradeVolumeScore;

    @Column(name = "cultural_exchange_score")
    private Double culturalExchangeScore;

    @Column(name = "evidence_strength")
    private Double evidenceStrength;

    @Column(name = "historical_sources", columnDefinition = "text")
    private String historicalSources;

    @Column(name = "archaeological_note", columnDefinition = "text")
    private String archaeologicalNote;

    @Column(name = "route_quality")
    private String routeQuality;

    @Column(name = "num_archaeological_sites")
    private Integer numArchaeologicalSites;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
