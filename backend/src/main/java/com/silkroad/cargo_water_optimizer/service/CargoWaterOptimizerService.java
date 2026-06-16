package com.silkroad.cargo_water_optimizer.service;

import com.silkroad.dto.CargoWaterOptimizationDTO;
import com.silkroad.entity.CargoWaterConfig;
import com.silkroad.repository.CargoWaterConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CargoWaterOptimizerService {

    private final CargoWaterConfigRepository cargoWaterConfigRepository;

    private static final String GENERAL_CARGO_TYPE = "GENERAL";

    public CargoWaterOptimizationDTO optimize(String cargoType, Integer camelCount, Integer crewCount,
                                               Double cargoWeightKg, String terrainType, Double temperatureC) {
        CargoWaterConfig config = getConfig(cargoType);

        double optimalCargoKg = camelCount * config.getOptimalCargoPerCamelKg();
        double maxCargoKg = camelCount * config.getMaxCargoPerCamelKg();
        double dailyWaterConsumption = calculateDailyWaterConsumption(
                config, camelCount, crewCount, cargoWeightKg, terrainType, temperatureC);

        double cargoWaterRatio = cargoWeightKg > 0 && dailyWaterConsumption > 0
                ? cargoWeightKg / dailyWaterConsumption
                : 0.0;

        double waterDaysEstimate = 0.0;
        double recommendedWaterCapacity = dailyWaterConsumption * 7;

        String suggestion = generateSuggestion(cargoWeightKg, optimalCargoKg, maxCargoKg,
                terrainType, temperatureC, dailyWaterConsumption, cargoWaterRatio);

        return CargoWaterOptimizationDTO.builder()
                .cargoType(config.getCargoType())
                .cargoName(config.getCargoName())
                .camelCount(camelCount)
                .crewCount(crewCount)
                .cargoWeightKg(cargoWeightKg)
                .optimalCargoKg(optimalCargoKg)
                .maxCargoKg(maxCargoKg)
                .dailyWaterConsumptionLiters(dailyWaterConsumption)
                .dailyCargoWaterRatio(cargoWaterRatio)
                .waterDaysEstimate(waterDaysEstimate)
                .recommendedWaterCapacity(recommendedWaterCapacity)
                .suggestion(suggestion)
                .terrainType(terrainType)
                .temperatureC(temperatureC)
                .build();
    }

    public List<CargoWaterConfig> getAllCargoConfigs() {
        return cargoWaterConfigRepository.findAll();
    }

    public List<Map<String, Object>> simulateWaterConsumption(String cargoType, Integer camelCount,
                                                               Integer crewCount, Double cargoWeightKg,
                                                               Integer days, String terrainType, Double temperatureC) {
        List<Map<String, Object>> result = new ArrayList<>();
        CargoWaterConfig config = getConfig(cargoType);
        double dailyWater = calculateDailyWaterConsumption(
                config, camelCount, crewCount, cargoWeightKg, terrainType, temperatureC);
        double cumulativeWater = 0.0;

        for (int i = 1; i <= days; i++) {
            cumulativeWater += dailyWater;
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", i);
            dayData.put("dailyConsumptionLiters", dailyWater);
            dayData.put("cumulativeConsumptionLiters", cumulativeWater);
            result.add(dayData);
        }

        return result;
    }

    private CargoWaterConfig getConfig(String cargoType) {
        return cargoWaterConfigRepository.findByCargoType(cargoType)
                .orElseGet(() -> cargoWaterConfigRepository.findByCargoType(GENERAL_CARGO_TYPE)
                        .orElseGet(this::createDefaultConfig));
    }

    private CargoWaterConfig createDefaultConfig() {
        CargoWaterConfig config = new CargoWaterConfig();
        config.setCargoType(GENERAL_CARGO_TYPE);
        config.setCargoName("通用货物");
        config.setWaterPer100kgPerDayLiters(0.5);
        config.setCamelBaseWaterDailyL(40.0);
        config.setCrewBaseWaterDailyL(3.0);
        config.setTerrainFactorDesert(1.5);
        config.setTerrainFactorMountains(2.0);
        config.setTerrainFactorOasis(1.0);
        config.setTemperatureFactorPerDegree(0.02);
        config.setMaxCargoPerCamelKg(200.0);
        config.setOptimalCargoPerCamelKg(150.0);
        return config;
    }

    private double calculateDailyWaterConsumption(CargoWaterConfig config, Integer camelCount,
                                                   Integer crewCount, Double cargoWeightKg,
                                                   String terrainType, Double temperatureC) {
        double terrainFactor = getTerrainFactor(config, terrainType);
        double temperatureFactor = 1 + config.getTemperatureFactorPerDegree() * (temperatureC - 25);

        double camelWater = camelCount * config.getCamelBaseWaterDailyL();
        double crewWater = crewCount * config.getCrewBaseWaterDailyL();
        double cargoWater = (cargoWeightKg / 100) * config.getWaterPer100kgPerDayLiters();

        double baseConsumption = camelWater + crewWater + cargoWater;
        return baseConsumption * terrainFactor * temperatureFactor;
    }

    private double getTerrainFactor(CargoWaterConfig config, String terrainType) {
        if (terrainType == null) {
            return config.getTerrainFactorDesert();
        }
        switch (terrainType.toUpperCase()) {
            case "DESERT":
                return config.getTerrainFactorDesert();
            case "MOUNTAINS":
            case "HIGH_MOUNTAINS":
                return config.getTerrainFactorMountains();
            case "OASIS":
            case "VALLEY":
            case "STEPPE":
                return config.getTerrainFactorOasis();
            case "SAND_DUNES":
                return config.getTerrainFactorDesert() * 1.1;
            default:
                return config.getTerrainFactorDesert();
        }
    }

    private String generateSuggestion(Double cargoWeightKg, Double optimalCargoKg, Double maxCargoKg,
                                       String terrainType, Double temperatureC,
                                       double dailyWaterConsumption, double cargoWaterRatio) {
        List<String> suggestions = new ArrayList<>();

        if (cargoWeightKg > maxCargoKg) {
            double overMaxPct = (cargoWeightKg - maxCargoKg) / maxCargoKg * 100;
            suggestions.add(String.format("当前载重超出最大载重量%.0f%%，严重超载会导致骆驼受伤，请立即减少货物或增加骆驼数量",
                    overMaxPct));
        } else if (cargoWeightKg > optimalCargoKg) {
            double overOptPct = (cargoWeightKg - optimalCargoKg) / optimalCargoKg * 100;
            suggestions.add(String.format("当前载重超出最优值%.0f%%，建议增加骆驼或减少货物以降低水耗比",
                    overOptPct));
        } else if (cargoWeightKg < optimalCargoKg * 0.5) {
            suggestions.add("当前载重偏轻，运力未充分利用，可考虑适当增加货物");
        } else {
            suggestions.add("当前载重处于最优区间，经济性良好");
        }

        if (temperatureC != null && temperatureC > 35) {
            suggestions.add(String.format("当前温度%.0f°C偏高，水源消耗显著增加，建议多储备20%%以上的饮用水",
                    temperatureC));
        } else if (temperatureC != null && temperatureC < 5) {
            suggestions.add(String.format("当前温度%.0f°C较低，水源消耗减少，但需注意防止水源结冰",
                    temperatureC));
        }

        if (terrainType != null) {
            switch (terrainType.toUpperCase()) {
                case "DESERT":
                    suggestions.add("当前地形为沙漠，水源稀缺，建议规划好水源补给点");
                    break;
                case "SAND_DUNES":
                    suggestions.add("当前地形为沙丘，行进困难且水耗高，建议选择绕行路线");
                    break;
                case "MOUNTAINS":
                case "HIGH_MOUNTAINS":
                    suggestions.add("当前地形为山地，水耗较高，注意队伍体力分配");
                    break;
                case "OASIS":
                    suggestions.add("当前地形为绿洲，可在此处补充水源和休整");
                    break;
            }
        }

        if (cargoWaterRatio > 0 && cargoWaterRatio < 3) {
            suggestions.add("载重水耗比较低，运输经济性欠佳，建议优化载重配置");
        } else if (cargoWaterRatio >= 5) {
            suggestions.add("载重水耗比优秀，运输经济性良好");
        }

        return String.join("；", suggestions);
    }
}
