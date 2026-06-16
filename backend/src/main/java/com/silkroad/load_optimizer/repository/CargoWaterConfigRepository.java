package com.silkroad.load_optimizer.repository;

import com.silkroad.load_optimizer.entity.CargoWaterConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CargoWaterConfigRepository extends JpaRepository<CargoWaterConfig, Long> {

    Optional<CargoWaterConfig> findByCargoType(String cargoType);
}
