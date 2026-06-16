package com.silkroad;

import com.silkroad.cargo_water_optimizer.service.CargoWaterOptimizerService;
import com.silkroad.dto.CargoWaterOptimizationDTO;
import com.silkroad.entity.CargoWaterConfig;
import com.silkroad.repository.CargoWaterConfigRepository;
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
class CargoWaterOptimizerServiceTest {

    @Mock
    private CargoWaterConfigRepository cargoWaterConfigRepository;

    @InjectMocks
    private CargoWaterOptimizerService cargoWaterOptimizerService;

    private CargoWaterConfig silkConfig;
    private CargoWaterConfig generalConfig;

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
    }

    @Test
    void optimize_standardDesertConfig_returnsCorrectDailyWater() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);

        double expectedBase = 10 * 30.0 + 5 * 12.0 + (1500.0 / 100) * 1.5;
        double expectedDaily = expectedBase * 1.8 * 1.0;

        assertThat(result).isNotNull();
        assertThat(result.getDailyWaterConsumptionLiters()).isEqualTo(expectedDaily);
    }

    @Test
    void optimize_optimalCargo_shouldSuggestGood() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).containsAnyOf("最优区间", "经济性良好");
    }

    @Test
    void optimize_summerHighTemperature_shouldIncreaseWater() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO normalTemp = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);
        CargoWaterOptimizationDTO highTemp = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 45.0);

        assertThat(highTemp.getDailyWaterConsumptionLiters()).isGreaterThan(normalTemp.getDailyWaterConsumptionLiters());
        assertThat(highTemp.getSuggestion()).contains("温度偏高");
    }

    @Test
    void optimize_mountainTerrain_shouldApplyMountainFactor() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO mountainResult = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "MOUNTAINS", 25.0);
        CargoWaterOptimizationDTO oasisResult = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "OASIS", 25.0);

        assertThat(mountainResult.getDailyWaterConsumptionLiters()).isGreaterThan(oasisResult.getDailyWaterConsumptionLiters());
    }

    @Test
    void simulateWaterConsumption_15days_shouldBeCumulative() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        List<Map<String, Object>> result = cargoWaterOptimizerService.simulateWaterConsumption(
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

        List<CargoWaterConfig> result = cargoWaterOptimizerService.getAllCargoConfigs();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
    }

    @Test
    void optimize_maxCargoOverload_shouldWarnSevere() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 3000.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).containsAnyOf("严重超载", "超出最大");
    }

    @Test
    void optimize_overOptimalButUnderMax_shouldSuggestAddCamel() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1800.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).contains("建议增加骆驼");
    }

    @Test
    void optimize_veryLightCargo_shouldSuggestIncrease() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 450.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getSuggestion()).contains("运力未充分利用");
    }

    @Test
    void optimize_sandDunesTerrain_shouldUse110PercentOfDesertFactor() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO desertResult = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);
        CargoWaterOptimizationDTO sandDunesResult = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "SAND_DUNES", 25.0);

        double expectedRatio = 1.1;
        assertThat(sandDunesResult.getDailyWaterConsumptionLiters())
                .isEqualTo(desertResult.getDailyWaterConsumptionLiters() * expectedRatio);
    }

    @Test
    void optimize_freezingTemperature_shouldReduceWater() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO normalTemp = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);
        CargoWaterOptimizationDTO freezingTemp = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", -10.0);

        assertThat(freezingTemp.getDailyWaterConsumptionLiters()).isLessThan(normalTemp.getDailyWaterConsumptionLiters());
        assertThat(freezingTemp.getSuggestion()).contains("防止结冰");
    }

    @Test
    void optimize_unknownCargoType_shouldFallbackToGeneral() {
        when(cargoWaterConfigRepository.findByCargoType("UNKNOWN_TYPE")).thenReturn(Optional.empty());
        when(cargoWaterConfigRepository.findByCargoType("GENERAL")).thenReturn(Optional.of(generalConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "UNKNOWN_TYPE", 10, 5, 1500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result.getCargoType()).isEqualTo("GENERAL");
    }

    @Test
    void optimize_nullTerrain_shouldTreatAsDesert() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO nullTerrainResult = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, null, 25.0);
        CargoWaterOptimizationDTO desertResult = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, 1500.0, "DESERT", 25.0);

        assertThat(nullTerrainResult.getDailyWaterConsumptionLiters())
                .isEqualTo(desertResult.getDailyWaterConsumptionLiters());
    }

    @Test
    void optimize_nullCamelCount_shouldNotCrash() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "SILK", null, 5, 1500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
    }

    @Test
    void optimize_negativeCargoWeight_shouldHandleGracefully() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        CargoWaterOptimizationDTO result = cargoWaterOptimizerService.optimize(
                "SILK", 10, 5, -500.0, "DESERT", 25.0);

        assertThat(result).isNotNull();
    }

    @Test
    void simulateWaterConsumption_zeroDays_returnsEmpty() {
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(silkConfig));

        List<Map<String, Object>> result = cargoWaterOptimizerService.simulateWaterConsumption(
                "SILK", 10, 5, 1500.0, 0, "DESERT", 25.0);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
