package com.silkroad.repository;

import com.silkroad.entity.HistoricalSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricalSourceRepository extends JpaRepository<HistoricalSource, Long> {

    List<HistoricalSource> findByDynasty(String dynasty);

    List<HistoricalSource> findBySourceType(String sourceType);
}
