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
@Table(name = "historical_sources")
public class HistoricalSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String author;

    private String dynasty;

    @Column(name = "year_written")
    private Integer yearWritten;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "content_excerpt", columnDefinition = "text")
    private String contentExcerpt;

    @Column(name = "reliability_score")
    private Double reliabilityScore;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
