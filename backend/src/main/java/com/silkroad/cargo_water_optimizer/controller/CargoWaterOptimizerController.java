package com.silkroad.cargo_water_optimizer.controller;

import com.silkroad.cargo_water_optimizer.service.CargoWaterOptimizerService;
import com.silkroad.dto.CargoWaterOptimizationDTO;
import com.silkroad.dto.CargoWaterOptimizeRequest;
import com.silkroad.dto.CargoWaterSimulateRequest;
import com.silkroad.entity.CargoWaterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cargo-water")
@RequiredArgsConstructor
public class CargoWaterOptimizerController {

    private final CargoWaterOptimizerService cargoWaterOptimizerService;

    @GetMapping("/configs")
    public List<CargoWaterConfig> getAllCargoConfigs() {
        return cargoWaterOptimizerService.getAllCargoConfigs();
    }

    @PostMapping("/optimize")
    public CargoWaterOptimizationDTO optimize(@RequestBody CargoWaterOptimizeRequest request) {
        String terrainType = request.getTerrainType() != null ? request.getTerrainType() : "DESERT";
        Double temperatureC = request.getTemperatureC() != null ? request.getTemperatureC() : 25.0;

        return cargoWaterOptimizerService.optimize(
                request.getCargoType(),
                request.getCamelCount(),
                request.getCrewCount(),
                request.getCargoWeightKg(),
                terrainType,
                temperatureC
        );
    }

    @PostMapping("/simulate")
    public List<Map<String, Object>> simulate(@RequestBody CargoWaterSimulateRequest request) {
        String terrainType = request.getTerrainType() != null ? request.getTerrainType() : "DESERT";
        Double temperatureC = request.getTemperatureC() != null ? request.getTemperatureC() : 25.0;

        return cargoWaterOptimizerService.simulateWaterConsumption(
                request.getCargoType(),
                request.getCamelCount(),
                request.getCrewCount(),
                request.getCargoWeightKg(),
                request.getDays(),
                terrainType,
                temperatureC
        );
    }
}
