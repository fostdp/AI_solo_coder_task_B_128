package com.silkroad.route_evolution.repository;

import com.silkroad.route_evolution.entity.DynastyRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynastyRouteRepository extends JpaRepository<DynastyRoute, Long> {

    List<DynastyRoute> findByDynasty(String dynasty);

    List<DynastyRoute> findAllByOrderByStartYearAsc();
}
