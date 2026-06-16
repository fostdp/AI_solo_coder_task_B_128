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
@Table(name = "archaeological_sites")
public class ArchaeologicalSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name", nullable = false)
    private String siteName;

    @Column(name = "site_name_en")
    private String siteNameEn;

    @Column(name = "site_type")
    private String siteType;

    private String dynasty;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    @Column(name = "discovery_year")
    private Integer discoveryYear;

    @Column(name = "excavated_area_sqm")
    private Double excavatedAreaSqm;

    @Column(name = "cultural_remains", columnDefinition = "text")
    private String culturalRemains;

    @Column(name = "evidence_strength")
    private Double evidenceStrength;

    private String status;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
