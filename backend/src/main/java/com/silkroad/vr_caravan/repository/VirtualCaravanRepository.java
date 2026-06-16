package com.silkroad.vr_caravan.repository;

import com.silkroad.vr_caravan.entity.VirtualCaravan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VirtualCaravanRepository extends JpaRepository<VirtualCaravan, Long> {

    List<VirtualCaravan> findBySessionId(String sessionId);

    List<VirtualCaravan> findByStatus(String status);

    List<VirtualCaravan> findByIsPublicTrue();
}
