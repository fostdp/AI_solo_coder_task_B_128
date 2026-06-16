package com.silkroad.load_optimizer.controller;

import com.silkroad.load_optimizer.service.LoadOptimizerService;
import com.silkroad.load_optimizer.dto.CargoWaterOptimizationDTO;
import com.silkroad.load_optimizer.dto.CargoWaterOptimizeRequest;
import com.silkroad.load_optimizer.dto.CargoWaterSimulateRequest;
import com.silkroad.load_optimizer.entity.CamelType;
import com.silkroad.load_optimizer.entity.CargoWaterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cargo-water")
@RequiredArgsConstructor
public class LoadOptimizerController {

    private final LoadOptimizerService loadOptimizerService;

    @GetMapping("/configs")
    public List<CargoWaterConfig> getAllCargoConfigs() {
        return loadOptimizerService.getAllCargoConfigs();
    }

    @GetMapping("/camel-types")
    public List<CamelType> getAllCamelTypes() {
        return loadOptimizerService.getAllCamelTypes();
    }

    @PostMapping("/optimize")
    public CargoWaterOptimizationDTO optimize(@RequestBody CargoWaterOptimizeRequest request) {
        String terrainType = request.getTerrainType() != null ? request.getTerrainType() : "DESERT";
        Double temperatureC = request.getTemperatureC() != null ? request.getTemperatureC() : 25.0;

        return loadOptimizerService.optimize(
                request.getCargoType(),
                request.getCamelCount(),
                request.getCrewCount(),
                request.getCargoWeightKg(),
                terrainType,
                temperatureC,
                request.getCamelType()
        );
    }

    @PostMapping("/simulate")
    public List<Map<String, Object>> simulate(@RequestBody CargoWaterSimulateRequest request) {
        String terrainType = request.getTerrainType() != null ? request.getTerrainType() : "DESERT";
        Double temperatureC = request.getTemperatureC() != null ? request.getTemperatureC() : 25.0;

        return loadOptimizerService.simulateWaterConsumption(
                request.getCargoType(),
                request.getCamelCount(),
                request.getCrewCount(),
                request.getCargoWeightKg(),
                request.getDays(),
                terrainType,
                temperatureC,
                request.getCamelType()
        );
    }
}
