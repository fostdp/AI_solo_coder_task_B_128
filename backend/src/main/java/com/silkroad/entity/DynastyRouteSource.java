package com.silkroad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dynasty_route_sources")
public class DynastyRouteSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "relevance_note", columnDefinition = "text")
    private String relevanceNote;
}
