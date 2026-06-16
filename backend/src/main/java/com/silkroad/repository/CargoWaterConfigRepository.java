package com.silkroad.repository;

import com.silkroad.entity.CargoWaterConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CargoWaterConfigRepository extends JpaRepository<CargoWaterConfig, Long> {

    Optional<CargoWaterConfig> findByCargoType(String cargoType);
}
