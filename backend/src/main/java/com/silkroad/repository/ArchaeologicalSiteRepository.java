package com.silkroad.repository;

import com.silkroad.entity.ArchaeologicalSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchaeologicalSiteRepository extends JpaRepository<ArchaeologicalSite, Long> {

    List<ArchaeologicalSite> findBySiteType(String siteType);

    List<ArchaeologicalSite> findByDynasty(String dynasty);
}
