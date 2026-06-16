package com.silkroad.repository;

import com.silkroad.entity.CaravanJourneyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaravanJourneyEventRepository extends JpaRepository<CaravanJourneyEvent, Long> {

    List<CaravanJourneyEvent> findByVirtualCaravanIdOrderByEventTimeDesc(Long virtualCaravanId);

    List<CaravanJourneyEvent> findTop20ByVirtualCaravanIdOrderByEventTimeDesc(Long virtualCaravanId);
}
