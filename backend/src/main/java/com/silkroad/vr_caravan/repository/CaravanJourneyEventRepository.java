package com.silkroad.vr_caravan.repository;

import com.silkroad.vr_caravan.entity.CaravanJourneyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaravanJourneyEventRepository extends JpaRepository<CaravanJourneyEvent, Long> {

    List<CaravanJourneyEvent> findByVirtualCaravanIdOrderByEventTimeDesc(Long virtualCaravanId);

    List<CaravanJourneyEvent> findTop20ByVirtualCaravanIdOrderByEventTimeDesc(Long virtualCaravanId);
}
