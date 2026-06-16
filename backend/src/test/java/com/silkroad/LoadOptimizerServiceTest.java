package com.silkroad;

import com.silkroad.load_optimizer.dto.CargoWaterOptimizationDTO;
import com.silkroad.load_optimizer.entity.CamelType;
import com.silkroad.load_optimizer.entity.CargoWaterConfig;
import com.silkroad.load_optimizer.repository.CamelTypeRepository;
import com.silkroad.load_optimizer.repository.CargoWaterConfigRepository;
import com.silkroad.load_optimizer.service.LoadOptimizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadOptimizerServiceTest {

    @Mock
    private CargoWaterConfigRepository cargoWaterConfigRepository;

    @Mock
    private CamelTypeRepository camelTypeRepository;

    @InjectMocks
    private LoadOptimizerService loadOptimizerService;

    private CargoWaterConfig silkConfig;
    private CargoWaterConfig generalConfig;
    private CamelType bactrianCamel;
    private CamelType dromedaryCamel;

    @BeforeEach
    void setUp() {
        silkConfig = new CargoWaterConfig();
        silkConfig.setCargoType("SILK");
        silkConfig.setCargoName("丝绸");
        silkConfig.setWaterPer100kgPerDayLiters(1.5);
        silkConfig.setCamelBaseWaterDailyL(30.0);
        silkConfig.setCrewBaseWaterDailyL(12.0);
        silkConfig.setTerrainFactorDesert(1.8);
        silkConfig.setTerrainFactorMountains(1.5);
        silkConfig.setTerrainFactorOasis(0.8);
        silkConfig.setTemperatureFactorPerDegree(0.03);
        silkConfig.setMaxCargoPerCamelKg(200.0);
        silkConfig.setOptimalCargoPerCamelKg(150.0);

        generalConfig = new CargoWaterConfig();
        generalConfig.setCargoType("GENERAL");
        generalConfig.setCargoName("通用货物");
        generalConfig.setWaterPer100kgPerDayLiters(0.5);
        generalConfig.setCamelBaseWaterDailyL(40.0);
        generalConfig.setCrewBaseWaterDailyL(3.0);
        generalConfig.setTerrainFactorDesert(1.5);
        generalConfig.setTerrainFactorMountains(2.0);
        generalConfig.setTerrainFactorOasis(1.0);
        generalConfig.setTemperatureFactorPerDegree(0.02);
        generalConfig.setMaxCargoPerCamelKg(200.0);
        generalConfig.setOptimalCargoPerCamelKg(150.0);

        bactrianCamel = new CamelType();
        bactrianCamel.setTypeCode("BACTRIAN");
        bactrianCamel.setTypeName("双峰驼");
        bactrianCamel.setTypeNameEn("Bactrian Camel");
        bactrianCamel.setAvgBodyWeightKg(500.0);
        bactrianCamel.setBodyHeightM(2.1);
        bactrianCamel.setOptimalLoadRatio(0.3);
        bactrianCamel.setMaxLoadRatio(0.4);
        bactrianCamel.setBaseWaterPerKgBody(0.08);
        bactrianCamel.setWaterTempCoefficient(0.02);
        bactrianCamel.setBaseSpeedKmh(4.0);
        bactrianCamel.setLoadSpeedDecayFactor(0.001);
        bactrianCamel.setHeatResistanceScore(6.0);
        bactrianCamel.setColdResistanceScore(9.0);
        bactrianCamel.setStaminaScore(8.0);
        bactrianCamel.setDailyDistanceKm(30.0);

        dromedaryCamel = new CamelType();
        dromedaryCamel.setTypeCode("DROMEDARY");
        dromedaryCamel.setTypeName("单峰驼");
        dromedaryCamel.setTypeNameEn("Dromedary Camel");
        dromedaryCamel.setAvgBodyWeightKg(450.0);
        dromedaryCamel.setBodyHeightM(1.9);
        dromedaryCamel.setOptimalLoadRatio(0.25);
        dromedaryCamel.setMaxLoadRatio(0.35);
        dromedaryCamel.setBaseWaterPerKgBody(0.06);
        dromedaryCamel.setWaterTempCoefficient(0.03);
        dromedaryCamel.setBaseSpeedKmh(5.0);
        dromedaryCamel.setLoadSpeedDecayFactor(0.0015);
        dromedaryCamel.setHeatResistanceScore(9.0);
        dromedaryCamel.setColdResistanceScore(4.0);
        dromedaryCamel.setStaminaScore(7.0);
        dromedaryCamel.setDailyDistanceKm(35.0);
    }

    @Test
    void optimize_standardDesertConfig_returnsCorrectDailyWater() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);

        double expectedCamelWater = 10 * 500.0 * 0.08;
        double expectedCrewWater = 5 * 12.0;
        double expectedCargoWater = (1500.0 / 100) * 1.5;
        double expectedBase = expectedCamelWater + expectedCrewWater + expectedCargoWater;
        double expectedDaily = expectedBase * 1.8 * 1.0;

        assertThat(result).isNotNull();
        assertThat(result.getDailyWaterConsumptionLiters()).isEqualTo(expectedDaily);
    }

    @Test
    void optimize_optimalCargo_shouldSuggestGood() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).containsAnyOf("最优区间", "经济性良好");
    }

    @Test
    void optimize_summerHighTemperature_shouldIncreaseWater() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO normalTemp = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);
        CargoWaterOptimizationDTO highTemp = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 45.0);

        assertThat(highTemp.getDailyWaterConsumptionLiters()).isGreaterThan(normalTemp.getDailyWaterConsumptionLiters());
        assertThat(highTemp.getSuggestion()).contains("温度偏高");
    }

    @Test
    void optimize_mountainTerrain_shouldApplyMountainFactor() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO mountainResult = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "MOUNTAINS", 25.0);
        CargoWaterOptimizationDTO oasisResult = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "OASIS", 25.0);

        assertThat(mountainResult.getDailyWaterConsumptionLiters()).isGreaterThan(oasisResult.getDailyWaterConsumptionLiters());
    }

    @Test
    void simulateWaterConsumption_15days_shouldBeCumulative() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        List<Map<String, Object>> result = loadOptimizerService.simulateWaterConsumption(
                "SILK", 10, 5, 1500.0, 15, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(15);

        double day1Daily = (Double) result.get(0).get("dailyConsumptionLiters");
        double day15Cumulative = (Double) result.get(14).get("cumulativeConsumptionLiters");
        assertThat(day15Cumulative).isEqualTo(day1Daily * 15);
    }

    @Test
    void getAllCargoConfigs_shouldReturnAll() {
        CargoWaterConfig c1 = new CargoWaterConfig();
        c1.setCargoType("SPICE");
        CargoWaterConfig c2 = new CargoWaterConfig();
        c2.setCargoType("TEA");
        CargoWaterConfig c3 = new CargoWaterConfig();
        c3.setCargoType("PORCELAIN");

        when(cargoWaterConfigRepository.findAll()).thenReturn(Arrays.asList(c1, c2, c3));

        List<CargoWaterConfig> result = loadOptimizerService.getAllCargoConfigs();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
    }

    @Test
    void optimize_maxCargoOverload_shouldWarnSevere() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", 10, 5, 3000.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).containsAnyOf("严重超载", "超出最大");
    }

    @Test
    void optimize_overOptimalButUnderMax_shouldSuggestAddCamel() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", 10, 5, 1800.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).contains("建议增加骆驼");
    }

    @Test
    void optimize_veryLightCargo_shouldSuggestIncrease() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", 10, 5, 450.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).contains("运力未充分利用");
    }

    @Test
    void optimize_sandDunesTerrain_shouldUse110PercentOfDesertFactor() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO desertResult = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);
        CargoWaterOptimizationDTO sandDunesResult = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "SAND_DUNES", 25.0);

        double expectedRatio = 1.1;
        assertThat(sandDunesResult.getDailyWaterConsumptionLiters())
                .isEqualTo(desertResult.getDailyWaterConsumptionLiters() * expectedRatio);
    }

    @Test
    void optimize_freezingTemperature_shouldReduceWater() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO normalTemp = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);
        CargoWaterOptimizationDTO freezingTemp = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", -10.0);

        assertThat(freezingTemp.getDailyWaterConsumptionLiters()).isLessThan(normalTemp.getDailyWaterConsumptionLiters());
        assertThat(freezingTemp.getSuggestion()).contains("防止结冰");
    }

    @Test
    void optimize_unknownCargoType_shouldFallbackToGeneral() {
        when(cargoWaterConfigRepository.findByCargoType("UNKNOWN_TYPE")).thenReturn(Optional.empty());
        when(cargoWaterConfigRepository.findByCargoType("GENERAL")).thenReturn(Optional.of(generalConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "UNKNOWN_TYPE", 10, 5, 1500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getCargoType()).isEqualTo("GENERAL");
    }

    @Test
    void optimize_nullTerrain_shouldTreatAsDesert() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO nullTerrainResult = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, null, 25.0);
        CargoWaterOptimizationDTO desertResult = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);

        assertThat(nullTerrainResult.getDailyWaterConsumptionLiters())
                .isEqualTo(desertResult.getDailyWaterConsumptionLiters());
    }

    @Test
    void optimize_nullCamelCount_shouldNotCrash() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", null, 5, 1500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
    }

    @Test
    void optimize_negativeCargoWeight_shouldHandleGracefully() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", 10, 5, -500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
    }

    @Test
    void simulateWaterConsumption_zeroDays_returnsEmpty() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        List<Map<String, Object>> result = loadOptimizerService.simulateWaterConsumption(
                "SILK", 10, 5, 1500.0, 0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void optimize_withCamelType_shouldUseBiologicalParams() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("DROMEDARY")).thenReturn(Optional.of(dromedaryCamel));

        CargoWaterOptimizationDTO result = loadOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0, "DROMEDARY");

        assertThat(result).isNotNull();
        assertThat(result.getCamelType()).isEqualTo("DROMEDARY");
        assertThat(result.getCamelTypeName()).isEqualTo("单峰驼");
        assertThat(result.getBodyWeightKg()).isEqualTo(450.0);
        assertThat(result.getActualSpeedKmh()).isGreaterThan(0);

        double optimalCargoKg = 10 * 450.0 * 0.25;
        double maxCargoKg = 10 * 450.0 * 0.35;
        assertThat(result.getOptimalCargoKg()).isEqualTo(optimalCargoKg);
        assertThat(result.getMaxCargoKg()).isEqualTo(maxCargoKg);
    }

    @Test
    void optimize_overload_shouldReduceSpeed() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));
        when(camelTypeRepository.findByTypeCode("BACTRIAN")).thenReturn(Optional.of(bactrianCamel));

        CargoWaterOptimizationDTO normalLoad = loadOptimizerService.optimize(
                "SILK", 10, 5, 1000.0, "DESERT", 25.0);
        CargoWaterOptimizationDTO heavyLoad = loadOptimizerService.optimize(
                "SILK", 10, 5, 2500.0, "DESERT", 25.0);

        assertThat(normalLoad.getActualSpeedKmh()).isGreaterThan(heavyLoad.getActualSpeedKmh());

        double expectedNormalSpeed = 4.0 * (1 - 0.001 * 1000.0);
        double expectedHeavySpeed = 4.0 * (1 - 0.001 * 2500.0);
        if (expectedHeavySpeed < 0) expectedHeavySpeed = 0;

        assertThat(normalLoad.getActualSpeedKmh()).isEqualTo(expectedNormalSpeed);
        assertThat(heavyLoad.getActualSpeedKmh()).isEqualTo(expectedHeavySpeed);
    }

    @Test
    void getAllCamelTypes_shouldReturnAll() {
        CamelType c1 = new CamelType();
        c1.setTypeCode("BACTRIAN");
        c1.setTypeName("双峰驼");
        CamelType c2 = new CamelType();
        c2.setTypeCode("DROMEDARY");
        c2.setTypeName("单峰驼");
        CamelType c3 = new CamelType();
        c3.setTypeCode("HYBRID");
        c3.setTypeName("混血驼");

        when(camelTypeRepository.findAll()).thenReturn(Arrays.asList(c1, c2, c3));

        List<CamelType> result = loadOptimizerService.getAllCamelTypes();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTypeCode()).isEqualTo("BACTRIAN");
        assertThat(result.get(1).getTypeCode()).isEqualTo("DROMEDARY");
        assertThat(result.get(2).getTypeCode()).isEqualTo("HYBRID");
        verify(camelTypeRepository, times(1)).findAll();
    }
}
