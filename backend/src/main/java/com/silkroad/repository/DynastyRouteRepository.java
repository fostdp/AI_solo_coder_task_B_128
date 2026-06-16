package com.silkroad.repository;

import com.silkroad.entity.DynastyRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynastyRouteRepository extends JpaRepository<DynastyRoute, Long> {

    List<DynastyRoute> findByDynasty(String dynasty);

    List<DynastyRoute> findAllByOrderByStartYearAsc();
}
