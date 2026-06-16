package com.silkroad.route_comparator.repository;

import com.silkroad.route_comparator.entity.ModernRoad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModernRoadRepository extends JpaRepository<ModernRoad, Long> {

    List<ModernRoad> findByRoadType(String roadType);

    List<ModernRoad> findByCorrespondingAncientRouteId(Long routeId);
}
