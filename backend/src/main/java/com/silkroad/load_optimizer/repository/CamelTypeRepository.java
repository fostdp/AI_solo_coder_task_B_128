package com.silkroad.load_optimizer.repository;

import com.silkroad.load_optimizer.entity.CamelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CamelTypeRepository extends JpaRepository<CamelType, Long> {

    Optional<CamelType> findByTypeCode(String typeCode);
}
