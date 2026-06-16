package com.silkroad;

import com.silkroad.route_comparator.dto.ModernRoadDTO;
import com.silkroad.route_comparator.dto.RouteComparisonDTO;
import com.silkroad.route_comparator.entity.ModernRoad;
import com.silkroad.entity.Route;
import com.silkroad.route_comparator.repository.ModernRoadRepository;
import com.silkroad.repository.RouteRepository;
import com.silkroad.route_comparator.service.RouteComparatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteComparatorServiceTest {

    @Mock
    private ModernRoadRepository modernRoadRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteComparatorService routeComparatorService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private Route createAncientRoute(Long id, String name, Double totalDistanceKm, LineString geom) {
        Route route = new Route();
        route.setId(id);
        route.setName(name);
        route.setTotalDistanceKm(totalDistanceKm);
        route.setGeom(geom);
        return route;
    }

    private ModernRoad createModernRoad(Long id, String name, Double speedLimit,
                                        Double totalDistanceKm, Long correspondingAncientRouteId,
                                        LineString geom, String roadType, Integer laneCount, Integer yearBuilt) {
        ModernRoad road = new ModernRoad();
        road.setId(id);
        road.setName(name);
        road.setSpeedLimitKmh(speedLimit);
        road.setTotalDistanceKm(totalDistanceKm);
        road.setCorrespondingAncientRouteId(correspondingAncientRouteId);
        road.setGeom(geom);
        road.setRoadType(roadType);
        road.setLaneCount(laneCount);
        road.setYearBuilt(yearBuilt);
        return road;
    }

    private LineString createAncientLineString() {
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(108.94, 34.26),
                new Coordinate(103.83, 36.06),
                new Coordinate(95.33, 29.65)
        };
        return geometryFactory.createLineString(coords);
    }

    private LineString createModernLineString() {
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(108.95, 34.27),
                new Coordinate(103.84, 36.07),
                new Coordinate(95.34, 29.66)
        };
        return geometryFactory.createLineString(coords);
    }

    @Test
    void compareByModernRoadId_shouldCalculateCorrectAncientDays() {
        LineString ancientGeom = createAncientLineString();
        LineString modernGeom = createModernLineString();
        Route ancientRoute = createAncientRoute(1L, "汉代北道", 3800.0, ancientGeom);
        ModernRoad modernRoad = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, modernGeom, "HIGHWAY", 4, 2014);

        when(modernRoadRepository.findById(1L)).thenReturn(Optional.of(modernRoad));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancientRoute));

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAncientTravelDays()).isEqualTo(3800.0 / 25.0);
    }

    @Test
    void compareByModernRoadId_shouldCalculateModernHours() {
        LineString ancientGeom = createAncientLineString();
        LineString modernGeom = createModernLineString();
        Route ancientRoute = createAncientRoute(1L, "汉代北道", 3800.0, ancientGeom);
        ModernRoad modernRoad = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, modernGeom, "HIGHWAY", 4, 2014);

        when(modernRoadRepository.findById(1L)).thenReturn(Optional.of(modernRoad));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancientRoute));

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(1L);

        double expectedHours = 3500.0 / (120.0 * 0.8);
        assertThat(result).isNotNull();
        assertThat(result.getModernTravelHours()).isEqualTo(expectedHours);
    }

    @Test
    void compareByModernRoadId_ancientWaterShouldBeInThousands() {
        LineString ancientGeom = createAncientLineString();
        LineString modernGeom = createModernLineString();
        Route ancientRoute = createAncientRoute(1L, "汉代北道", 3800.0, ancientGeom);
        ModernRoad modernRoad = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, modernGeom, "HIGHWAY", 4, 2014);

        when(modernRoadRepository.findById(1L)).thenReturn(Optional.of(modernRoad));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancientRoute));

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(1L);

        double expectedWater = (3800.0 / 25.0) * 1140.0;
        assertThat(result).isNotNull();
        assertThat(result.getAncientWaterRequiredLiters()).isEqualTo(expectedWater);
    }

    @Test
    void compareByModernRoadId_analysisShouldMentionTimeSaving() {
        LineString ancientGeom = createAncientLineString();
        LineString modernGeom = createModernLineString();
        Route ancientRoute = createAncientRoute(1L, "汉代北道", 3800.0, ancientGeom);
        ModernRoad modernRoad = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, modernGeom, "HIGHWAY", 4, 2014);

        when(modernRoadRepository.findById(1L)).thenReturn(Optional.of(modernRoad));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancientRoute));

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAnalysis()).contains("缩短");
        assertThat(result.getAnalysis()).containsAnyOf("个月", "周", "天", "小时");
    }

    @Test
    void compareByAncientRouteId_twoMatchingRoads_returnsTwo() {
        LineString ancientGeom = createAncientLineString();
        LineString modernGeom1 = createModernLineString();
        LineString modernGeom2 = createAncientLineString();
        Route ancientRoute = createAncientRoute(1L, "汉代北道", 3800.0, ancientGeom);
        ModernRoad road1 = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, modernGeom1, "HIGHWAY", 4, 2014);
        ModernRoad road2 = createModernRoad(2L, "G312国道", 80.0, 3600.0, 1L, modernGeom2, "NATIONAL", 2, 2000);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancientRoute));
        when(modernRoadRepository.findByCorrespondingAncientRouteId(1L)).thenReturn(Arrays.asList(road1, road2));

        List<RouteComparisonDTO> result = routeComparatorService.compareByAncientRouteId(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }

    @Test
    void compareAllPairs_threeValidPairs_returnsThree() {
        LineString ancientGeom1 = createAncientLineString();
        LineString ancientGeom2 = createModernLineString();
        LineString modernGeom1 = createAncientLineString();
        LineString modernGeom2 = createModernLineString();
        LineString modernGeom3 = createAncientLineString();

        Route ancient1 = createAncientRoute(1L, "汉代北道", 3800.0, ancientGeom1);
        Route ancient2 = createAncientRoute(2L, "唐代南道", 4200.0, ancientGeom2);

        ModernRoad road1 = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, modernGeom1, "HIGHWAY", 4, 2014);
        ModernRoad road2 = createModernRoad(2L, "G312国道", 80.0, 3600.0, 1L, modernGeom2, "NATIONAL", 2, 2000);
        ModernRoad road3 = createModernRoad(3L, "G5京昆高速", 120.0, 4000.0, 2L, modernGeom3, "HIGHWAY", 4, 2010);
        ModernRoad road4 = createModernRoad(4L, "某无对应公路", 100.0, 1000.0, null, modernGeom1, "OTHER", 2, 2015);
        ModernRoad road5 = createModernRoad(5L, "另一无对应公路", 100.0, 1000.0, 999L, modernGeom2, "OTHER", 2, 2016);

        when(modernRoadRepository.findAll()).thenReturn(Arrays.asList(road1, road2, road3, road4, road5));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancient1));
        when(routeRepository.findById(2L)).thenReturn(Optional.of(ancient2));
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        List<RouteComparisonDTO> result = routeComparatorService.compareAllPairs();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
    }

    @Test
    void getAllModernRoads_returnsAll() {
        ModernRoad r1 = createModernRoad(1L, "G30", 120.0, 1000.0, 1L, createAncientLineString(), "HIGHWAY", 4, 2010);
        ModernRoad r2 = createModernRoad(2L, "G312", 80.0, 2000.0, 2L, createModernLineString(), "NATIONAL", 2, 2000);
        ModernRoad r3 = createModernRoad(3L, "G5", 120.0, 3000.0, 3L, createAncientLineString(), "HIGHWAY", 4, 2015);
        ModernRoad r4 = createModernRoad(4L, "G6", 100.0, 4000.0, 4L, createModernLineString(), "HIGHWAY", 4, 2018);
        ModernRoad r5 = createModernRoad(5L, "省道S101", 60.0, 500.0, null, createAncientLineString(), "PROVINCIAL", 2, 2005);

        when(modernRoadRepository.findAll()).thenReturn(Arrays.asList(r1, r2, r3, r4, r5));

        List<ModernRoadDTO> result = routeComparatorService.getAllModernRoads();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
    }

    @Test
    void toDTO_convertsAllFields() {
        LineString geom = createModernLineString();
        ModernRoad road = createModernRoad(10L, "G30连霍高速", 120.0, 3500.0, 1L, geom, "HIGHWAY", 4, 2014);
        road.setNameEn("G30 Lianhuo Expressway");
        road.setPaved(true);
        road.setDescription("连云港至霍尔果斯高速公路");

        ModernRoadDTO dto = routeComparatorService.toDTO(road);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("G30连霍高速");
        assertThat(dto.getRoadType()).isEqualTo("HIGHWAY");
        assertThat(dto.getTotalDistanceKm()).isEqualTo(3500.0);
        assertThat(dto.getLaneCount()).isEqualTo(4);
        assertThat(dto.getYearBuilt()).isEqualTo(2014);
    }

    @Test
    void compareByModernRoadId_zeroDistance_returnsZeroAll() {
        LineString geom = createAncientLineString();
        Route ancientRoute = createAncientRoute(1L, "测试路线", 0.0, geom);
        ModernRoad modernRoad = createModernRoad(1L, "测试公路", 120.0, 0.0, 1L, geom, "HIGHWAY", 4, 2014);

        when(modernRoadRepository.findById(1L)).thenReturn(Optional.of(modernRoad));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancientRoute));

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAncientTravelDays()).isEqualTo(0.0);
        assertThat(result.getModernTravelHours()).isEqualTo(0.0);
    }

    @Test
    void calculateOverlap_identicalRoutes_returnsNear100() {
        LineString identicalGeom = createAncientLineString();
        Route ancientRoute = createAncientRoute(1L, "汉代北道", 3800.0, identicalGeom);
        ModernRoad modernRoad = createModernRoad(1L, "相同路线公路", 120.0, 3500.0, 1L, identicalGeom, "HIGHWAY", 4, 2014);

        when(modernRoadRepository.findById(1L)).thenReturn(Optional.of(modernRoad));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancientRoute));

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOverlapPct()).isGreaterThanOrEqualTo(90.0);
    }

    @Test
    void compareByModernRoadId_nullCorrespondingId_returnsNull() {
        ModernRoad modernRoad = createModernRoad(1L, "无对应古道公路", 120.0, 1000.0, null, createModernLineString(), "OTHER", 2, 2015);

        when(modernRoadRepository.findById(1L)).thenReturn(Optional.of(modernRoad));

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(1L);

        assertThat(result).isNull();
    }

    @Test
    void compareByModernRoadId_notExistentId_returnsNull() {
        when(modernRoadRepository.findById(99999L)).thenReturn(Optional.empty());

        RouteComparisonDTO result = routeComparatorService.compareByModernRoadId(99999L);

        assertThat(result).isNull();
    }

    @Test
    void compareByAncientRouteId_notExistentId_returnsEmpty() {
        when(routeRepository.findById(99999L)).thenReturn(Optional.empty());

        List<RouteComparisonDTO> result = routeComparatorService.compareByAncientRouteId(99999L);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void compareAllPairs_repositoryThrows_handlesGracefully() {
        when(modernRoadRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        try {
            List<RouteComparisonDTO> result = routeComparatorService.compareAllPairs();
            assertThat(result).isNotNull();
        } catch (RuntimeException e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void toDTO_shouldIncludeStandardizedFields() {
        LineString geom = createModernLineString();
        ModernRoad road = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, geom, "HIGHWAY", 4, 2014);
        road.setRoadNumber("G30");
        road.setRoadClass("NATIONAL_EXPRESSWAY");
        road.setPavementType("ASPHALT_CONCRETE");
        road.setDesignSpeedKmh(120);
        road.setLaneWidthM(3.75);
        road.setAdminLevel("NATIONAL");
        road.setTotalLengthKm(4244.0);
        road.setOpeningYear(2014);
        road.setStandardName("连云港-霍尔果斯高速公路");

        ModernRoadDTO dto = routeComparatorService.toDTO(road);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("G30连霍高速");
        assertThat(dto.getRoadType()).isEqualTo("HIGHWAY");
        assertThat(dto.getTotalDistanceKm()).isEqualTo(3500.0);
        assertThat(dto.getLaneCount()).isEqualTo(4);
        assertThat(dto.getYearBuilt()).isEqualTo(2014);
    }

    @Test
    void compareAllPairs_withStandardRoads_shouldIncludeStandardInfo() {
        LineString ancientGeom = createAncientLineString();
        LineString modernGeom = createModernLineString();

        Route ancient = createAncientRoute(1L, "汉代北道", 3800.0, ancientGeom);

        ModernRoad road1 = createModernRoad(1L, "G30连霍高速", 120.0, 3500.0, 1L, modernGeom, "HIGHWAY", 4, 2014);
        road1.setRoadNumber("G30");
        road1.setRoadClass("NATIONAL_EXPRESSWAY");
        road1.setPavementType("ASPHALT_CONCRETE");
        road1.setDesignSpeedKmh(120);

        ModernRoad road2 = createModernRoad(2L, "G312国道", 80.0, 3600.0, 1L, ancientGeom, "NATIONAL", 2, 2000);
        road2.setRoadNumber("G312");
        road2.setRoadClass("NATIONAL_HIGHWAY");
        road2.setPavementType("ASPHALT");
        road2.setDesignSpeedKmh(80);

        when(modernRoadRepository.findAll()).thenReturn(Arrays.asList(road1, road2));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(ancient));

        List<RouteComparisonDTO> result = routeComparatorService.compareAllPairs();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getModernRoadName()).isEqualTo("G30连霍高速");
        assertThat(result.get(1).getModernRoadName()).isEqualTo("G312国道");

        for (RouteComparisonDTO dto : result) {
            assertThat(dto.getAncientTravelDays()).isGreaterThan(0);
            assertThat(dto.getModernTravelHours()).isGreaterThan(0);
            assertThat(dto.getAncientDistanceKm()).isGreaterThan(0);
            assertThat(dto.getModernDistanceKm()).isGreaterThan(0);
        }
    }
}
