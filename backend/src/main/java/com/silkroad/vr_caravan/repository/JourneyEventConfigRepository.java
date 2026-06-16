package com.silkroad.vr_caravan.repository;

import com.silkroad.vr_caravan.entity.JourneyEventConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourneyEventConfigRepository extends JpaRepository<JourneyEventConfig, Long> {

    List<JourneyEventConfig> findByIsPositive(Boolean isPositive);
}
