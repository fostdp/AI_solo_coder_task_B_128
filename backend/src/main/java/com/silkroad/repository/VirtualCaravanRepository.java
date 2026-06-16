package com.silkroad.repository;

import com.silkroad.entity.VirtualCaravan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VirtualCaravanRepository extends JpaRepository<VirtualCaravan, Long> {

    List<VirtualCaravan> findBySessionId(String sessionId);

    List<VirtualCaravan> findByStatus(String status);

    List<VirtualCaravan> findByIsPublicTrue();
}
