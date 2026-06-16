package com.silkroad.repository;

import com.silkroad.entity.ModernRoad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModernRoadRepository extends JpaRepository<ModernRoad, Long> {

    List<ModernRoad> findByRoadType(String roadType);

    List<ModernRoad> findByCorrespondingAncientRouteId(Long routeId);
}
