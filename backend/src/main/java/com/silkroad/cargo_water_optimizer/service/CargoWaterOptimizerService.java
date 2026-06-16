package com.silkroad.cargo_water_optimizer.service;

import com.silkroad.dto.CargoWaterOptimizationDTO;
import com.silkroad.entity.CamelType;
import com.silkroad.entity.CargoWaterConfig;
import com.silkroad.repository.CamelTypeRepository;
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
    private final CamelTypeRepository camelTypeRepository;

    private static final String GENERAL_CARGO_TYPE = "GENERAL";
    private static final String DEFAULT_CAMEL_TYPE = "BACTRIAN";

    public List<CamelType> getAllCamelTypes() {
        return camelTypeRepository.findAll();
    }

    public CargoWaterOptimizationDTO optimize(String cargoType, Integer camelCount, Integer crewCount,
                                               Double cargoWeightKg, String terrainType, Double temperatureC) {
        return optimize(cargoType, camelCount, crewCount, cargoWeightKg, terrainType, temperatureC, null);
    }

    public CargoWaterOptimizationDTO optimize(String cargoType, Integer camelCount, Integer crewCount,
                                               Double cargoWeightKg, String terrainType, Double temperatureC,
                                               String camelType) {
        CargoWaterConfig config = getConfig(cargoType);
        CamelType camel = getCamelType(camelType);

        double avgBodyWeightKg = camel.getAvgBodyWeightKg() != null ? camel.getAvgBodyWeightKg() : 500.0;
        double optimalLoadRatio = camel.getOptimalLoadRatio() != null ? camel.getOptimalLoadRatio() : 0.3;
        double maxLoadRatio = camel.getMaxLoadRatio() != null ? camel.getMaxLoadRatio() : 0.4;
        double baseSpeedKmh = camel.getBaseSpeedKmh() != null ? camel.getBaseSpeedKmh() : 4.0;
        double loadSpeedDecayFactor = camel.getLoadSpeedDecayFactor() != null ? camel.getLoadSpeedDecayFactor() : 0.001;
        double baseWaterPerKgBody = camel.getBaseWaterPerKgBody() != null ? camel.getBaseWaterPerKgBody() : 0.08;

        double optimalCargoKg = camelCount * avgBodyWeightKg * optimalLoadRatio;
        double maxCargoKg = camelCount * avgBodyWeightKg * maxLoadRatio;

        double actualSpeedKmh = baseSpeedKmh * (1 - loadSpeedDecayFactor * cargoWeightKg);
        if (actualSpeedKmh < 0) {
            actualSpeedKmh = 0;
        }

        double dailyWaterConsumption = calculateDailyWaterConsumption(
                config, camel, camelCount, crewCount, cargoWeightKg, terrainType, temperatureC);

        double cargoWaterRatio = cargoWeightKg > 0 && dailyWaterConsumption > 0
                ? cargoWeightKg / dailyWaterConsumption
                : 0.0;

        double waterDaysEstimate = 0.0;
        double recommendedWaterCapacity = dailyWaterConsumption * 7;

        String suggestion = generateSuggestion(cargoWeightKg, optimalCargoKg, maxCargoKg,
                terrainType, temperatureC, dailyWaterConsumption, cargoWaterRatio, camel);

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
                .actualSpeedKmh(actualSpeedKmh)
                .camelType(camel.getTypeCode())
                .camelTypeName(camel.getTypeName())
                .bodyWeightKg(avgBodyWeightKg)
                .build();
    }

    private CamelType getCamelType(String camelTypeCode) {
        if (camelTypeCode == null || camelTypeCode.isEmpty()) {
            return camelTypeRepository.findByTypeCode(DEFAULT_CAMEL_TYPE)
                    .orElseGet(this::createDefaultCamelType);
        }
        return camelTypeRepository.findByTypeCode(camelTypeCode)
                .orElseGet(() -> camelTypeRepository.findByTypeCode(DEFAULT_CAMEL_TYPE)
                        .orElseGet(this::createDefaultCamelType));
    }

    private CamelType createDefaultCamelType() {
        CamelType camel = new CamelType();
        camel.setTypeCode(DEFAULT_CAMEL_TYPE);
        camel.setTypeName("双峰驼");
        camel.setTypeNameEn("Bactrian Camel");
        camel.setAvgBodyWeightKg(500.0);
        camel.setBodyHeightM(2.1);
        camel.setOptimalLoadRatio(0.3);
        camel.setMaxLoadRatio(0.4);
        camel.setBaseWaterPerKgBody(0.08);
        camel.setWaterTempCoefficient(0.02);
        camel.setBaseSpeedKmh(4.0);
        camel.setLoadSpeedDecayFactor(0.001);
        camel.setHeatResistanceScore(6.0);
        camel.setColdResistanceScore(9.0);
        camel.setStaminaScore(8.0);
        camel.setDailyDistanceKm(30.0);
        camel.setDescription("双峰驼适合低温沙漠环境，耐寒能力强，是丝绸之路上的主要驮运工具");
        camel.setOriginRegion("中亚");
        return camel;
    }

    public List<CargoWaterConfig> getAllCargoConfigs() {
        return cargoWaterConfigRepository.findAll();
    }

    public List<Map<String, Object>> simulateWaterConsumption(String cargoType, Integer camelCount,
                                                               Integer crewCount, Double cargoWeightKg,
                                                               Integer days, String terrainType, Double temperatureC) {
        return simulateWaterConsumption(cargoType, camelCount, crewCount, cargoWeightKg, days, terrainType, temperatureC, null);
    }

    public List<Map<String, Object>> simulateWaterConsumption(String cargoType, Integer camelCount,
                                                               Integer crewCount, Double cargoWeightKg,
                                                               Integer days, String terrainType, Double temperatureC,
                                                               String camelType) {
        List<Map<String, Object>> result = new ArrayList<>();
        CargoWaterConfig config = getConfig(cargoType);
        CamelType camel = getCamelType(camelType);
        double dailyWater = calculateDailyWaterConsumption(
                config, camel, camelCount, crewCount, cargoWeightKg, terrainType, temperatureC);
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

    private double calculateDailyWaterConsumption(CargoWaterConfig config, CamelType camel,
                                                   Integer camelCount, Integer crewCount,
                                                   Double cargoWeightKg, String terrainType,
                                                   Double temperatureC) {
        double terrainFactor = getTerrainFactor(config, terrainType);
        double waterTempCoefficient = camel.getWaterTempCoefficient() != null
                ? camel.getWaterTempCoefficient()
                : config.getTemperatureFactorPerDegree();
        double temperatureFactor = 1 + waterTempCoefficient * (temperatureC - 25);

        double avgBodyWeightKg = camel.getAvgBodyWeightKg() != null ? camel.getAvgBodyWeightKg() : 500.0;
        double baseWaterPerKgBody = camel.getBaseWaterPerKgBody() != null ? camel.getBaseWaterPerKgBody() : 0.08;

        double camelWater = camelCount * avgBodyWeightKg * baseWaterPerKgBody;
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
                                       double dailyWaterConsumption, double cargoWaterRatio,
                                       CamelType camel) {
        List<String> suggestions = new ArrayList<>();

        if (camel != null && camel.getDescription() != null) {
            suggestions.add(camel.getDescription());
        }

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
