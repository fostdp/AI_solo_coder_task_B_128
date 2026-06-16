package com.silkroad.route_evolution.repository;

import com.silkroad.route_evolution.entity.ArchaeologicalSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchaeologicalSiteRepository extends JpaRepository<ArchaeologicalSite, Long> {

    List<ArchaeologicalSite> findBySiteType(String siteType);

    List<ArchaeologicalSite> findByDynasty(String dynasty);
}
