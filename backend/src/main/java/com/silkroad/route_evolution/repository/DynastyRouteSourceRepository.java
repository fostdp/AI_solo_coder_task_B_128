package com.silkroad.route_evolution.repository;

import com.silkroad.route_evolution.entity.DynastyRouteSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynastyRouteSourceRepository extends JpaRepository<DynastyRouteSource, Long> {

    List<DynastyRouteSource> findByRouteId(Long routeId);

    List<DynastyRouteSource> findBySourceId(Long sourceId);
}
