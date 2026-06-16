package com.silkroad.repository;

import com.silkroad.entity.DynastyRouteSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynastyRouteSourceRepository extends JpaRepository<DynastyRouteSource, Long> {

    List<DynastyRouteSource> findByRouteId(Long routeId);

    List<DynastyRouteSource> findBySourceId(Long sourceId);
}
