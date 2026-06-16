package com.silkroad;

import com.silkroad.route_evolution.dto.DynastyComparisonDTO;
import com.silkroad.route_evolution.dto.DynastyRouteDTO;
import com.silkroad.route_evolution.entity.ArchaeologicalSite;
import com.silkroad.route_evolution.entity.DynastyRoute;
import com.silkroad.route_evolution.entity.HistoricalSource;
import com.silkroad.route_evolution.repository.ArchaeologicalSiteRepository;
import com.silkroad.route_evolution.repository.DynastyRouteRepository;
import com.silkroad.route_evolution.repository.HistoricalSourceRepository;
import com.silkroad.route_evolution.service.RouteEvolutionService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteEvolutionServiceTest {

    @Mock
    private DynastyRouteRepository dynastyRouteRepository;

    @Mock
    private ArchaeologicalSiteRepository archaeologicalSiteRepository;

    @Mock
    private HistoricalSourceRepository historicalSourceRepository;

    @InjectMocks
    private RouteEvolutionService routeEvolutionService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private DynastyRoute createDynastyRoute(Long id, String dynasty, String dynastyName,
                                            Integer startYear, Integer endYear,
                                            Double politicalStability, Double tradeVolumeScore,
                                            Double culturalExchangeScore, Double totalDistanceKm,
                                            LineString geom) {
        DynastyRoute route = new DynastyRoute();
        route.setId(id);
        route.setDynasty(dynasty);
        route.setDynastyName(dynastyName);
        route.setName(dynastyName + "路线");
        route.setStartYear(startYear);
        route.setEndYear(endYear);
        route.setPoliticalStability(politicalStability);
        route.setTradeVolumeScore(tradeVolumeScore);
        route.setCulturalExchangeScore(culturalExchangeScore);
        route.setTotalDistanceKm(totalDistanceKm);
        route.setGeom(geom);
        return route;
    }

    private LineString createSampleLineString() {
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(108.94, 34.26),
                new Coordinate(103.83, 36.06),
                new Coordinate(95.33, 29.65)
        };
        return geometryFactory.createLineString(coords);
    }

    @Test
    void getAllDynasties_shouldGroupByDynastyAndSortChronologically() {
        LineString geom = createSampleLineString();

        DynastyRoute han = createDynastyRoute(1L, "HAN", "汉代", -202, 220, 0.7, 0.75, 0.7, 5000.0, geom);
        DynastyRoute tang = createDynastyRoute(2L, "TANG", "唐代", 618, 907, 0.85, 0.92, 0.88, 6500.0, geom);
        DynastyRoute song = createDynastyRoute(3L, "SONG", "宋代", 960, 1279, 0.78, 0.82, 0.85, 5800.0, geom);
        DynastyRoute yuan = createDynastyRoute(4L, "YUAN", "元代", 1271, 1368, 0.8, 0.88, 0.8, 7000.0, geom);
        DynastyRoute ming = createDynastyRoute(5L, "MING", "明代", 1368, 1644, 0.75, 0.8, 0.78, 6200.0, geom);

        when(dynastyRouteRepository.findAll()).thenReturn(Arrays.asList(ming, song, han, yuan, tang));

        List<DynastyComparisonDTO> result = routeEvolutionService.getAllDynasties();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getDynasty()).isEqualTo("HAN");
        assertThat(result.get(0).getStartYear()).isEqualTo(-202);
        assertThat(result.get(1).getDynasty()).isEqualTo("TANG");
        assertThat(result.get(2).getDynasty()).isEqualTo("SONG");
        assertThat(result.get(3).getDynasty()).isEqualTo("YUAN");
        assertThat(result.get(4).getDynasty()).isEqualTo("MING");

        for (DynastyComparisonDTO dto : result) {
            assertThat(dto.getRouteCount()).isEqualTo(1);
        }

        DynastyComparisonDTO tangDto = result.stream()
                .filter(d -> "TANG".equals(d.getDynasty()))
                .findFirst()
                .orElse(null);
        assertThat(tangDto).isNotNull();
        assertThat(tangDto.getAvgTradeVolume()).isGreaterThan(0.8);
    }

    @Test
    void getRoutesByDynasty_tangShouldReturnThreeRoutes() {
        LineString geom = createSampleLineString();
        DynastyRoute r1 = createDynastyRoute(1L, "TANG", "唐代", 618, 907, 0.8, 0.9, 0.85, 5000.0, geom);
        DynastyRoute r2 = createDynastyRoute(2L, "TANG", "唐代", 618, 907, 0.85, 0.92, 0.88, 5500.0, geom);
        DynastyRoute r3 = createDynastyRoute(3L, "TANG", "唐代", 618, 907, 0.82, 0.88, 0.86, 6000.0, geom);

        when(dynastyRouteRepository.findByDynasty("TANG")).thenReturn(Arrays.asList(r1, r2, r3));

        List<DynastyRouteDTO> result = routeEvolutionService.getRoutesByDynasty("TANG");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        for (DynastyRouteDTO dto : result) {
            assertThat(dto.getDynasty()).isEqualTo("TANG");
        }
    }

    @Test
    void compareDynasties_tangVsMing_shouldShowTangSuperior() {
        LineString geom = createSampleLineString();
        DynastyRoute tang1 = createDynastyRoute(1L, "TANG", "唐代", 618, 907, 0.9, 0.95, 0.9, 5000.0, geom);
        DynastyRoute tang2 = createDynastyRoute(2L, "TANG", "唐代", 618, 907, 0.92, 0.93, 0.88, 5500.0, geom);
        DynastyRoute song1 = createDynastyRoute(3L, "SONG", "宋代", 960, 1279, 0.6, 0.45, 0.55, 4500.0, geom);

        when(dynastyRouteRepository.findByDynasty("TANG")).thenReturn(Arrays.asList(tang1, tang2));
        when(dynastyRouteRepository.findByDynasty("SONG")).thenReturn(Collections.singletonList(song1));

        Map<String, DynastyComparisonDTO> result = routeEvolutionService.compareDynasties("TANG", "SONG");

        assertThat(result).isNotNull();
        assertThat(result).containsKeys("TANG", "SONG");

        DynastyComparisonDTO tang = result.get("TANG");
        DynastyComparisonDTO song = result.get("SONG");

        assertThat(tang.getRouteCount()).isEqualTo(2);
        assertThat(song.getRouteCount()).isEqualTo(1);
        assertThat(tang.getAvgTradeVolume()).isGreaterThan(0.9);
        assertThat(song.getAvgTradeVolume()).isLessThan(0.5);
        assertThat(tang.getAvgPoliticalStability()).isGreaterThan(song.getAvgPoliticalStability());
        assertThat(tang.getAvgTradeVolume()).isGreaterThan(song.getAvgTradeVolume());
        assertThat(tang.getAvgCulturalExchange()).isGreaterThan(song.getAvgCulturalExchange());
    }

    @Test
    void getDynastyTimeline_shouldSortByStartYear() {
        LineString geom = createSampleLineString();
        DynastyRoute ming = createDynastyRoute(1L, "MING", "明代", 1368, 1644, 0.7, 0.8, 0.75, 6000.0, geom);
        DynastyRoute han = createDynastyRoute(2L, "HAN", "汉代", -202, 220, 0.7, 0.75, 0.7, 5000.0, geom);
        DynastyRoute tang = createDynastyRoute(3L, "TANG", "唐代", 618, 907, 0.85, 0.9, 0.88, 6500.0, geom);

        when(dynastyRouteRepository.findAllByOrderByStartYearAsc()).thenReturn(Arrays.asList(han, tang, ming));

        List<DynastyRouteDTO> result = routeEvolutionService.getDynastyTimeline();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStartYear()).isEqualTo(-202);
        assertThat(result.get(1).getStartYear()).isEqualTo(618);
        assertThat(result.get(2).getStartYear()).isEqualTo(1368);
    }

    @Test
    void toDTO_shouldCorrectlyConvertCoordinates() {
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(108.94, 34.26),
                new Coordinate(103.83, 36.06),
                new Coordinate(95.33, 29.65)
        };
        LineString lineString = geometryFactory.createLineString(coords);

        DynastyRoute route = createDynastyRoute(1L, "TANG", "唐代", 618, 907,
                0.85, 0.92, 0.88, 6500.0, lineString);

        DynastyRouteDTO dto = routeEvolutionService.toDTO(route);

        assertThat(dto).isNotNull();
        assertThat(dto.getCoordinates()).isNotNull();
        assertThat(dto.getCoordinates()).hasSize(3);
        assertThat(dto.getCoordinates().get(0)[0]).isEqualTo(108.94);
        assertThat(dto.getCoordinates().get(0)[1]).isEqualTo(34.26);
        assertThat(dto.getCoordinates().get(1)[0]).isEqualTo(103.83);
        assertThat(dto.getCoordinates().get(1)[1]).isEqualTo(36.06);
        assertThat(dto.getCoordinates().get(2)[0]).isEqualTo(95.33);
        assertThat(dto.getCoordinates().get(2)[1]).isEqualTo(29.65);
    }

    @Test
    void buildComparisonDTO_shouldCalculateCorrectAverages() {
        LineString geom = createSampleLineString();
        DynastyRoute r1 = createDynastyRoute(1L, "TANG", "唐代", 618, 907, 0.9, 0.9, 0.8, 1000.0, geom);
        DynastyRoute r2 = createDynastyRoute(2L, "TANG", "唐代", 618, 907, 0.8, 0.9, 0.8, 2000.0, geom);
        DynastyRoute r3 = createDynastyRoute(3L, "TANG", "唐代", 618, 907, 0.7, 0.9, 0.8, 3000.0, geom);

        when(dynastyRouteRepository.findByDynasty("TANG")).thenReturn(Arrays.asList(r1, r2, r3));

        Map<String, DynastyComparisonDTO> result = routeEvolutionService.compareDynasties("TANG", "XIA");
        DynastyComparisonDTO dto = result.get("TANG");

        assertThat(dto).isNotNull();
        assertThat(dto.getAvgPoliticalStability()).isEqualTo(0.8);
        assertThat(dto.getAvgTradeVolume()).isEqualTo(0.9);
        assertThat(dto.getAvgCulturalExchange()).isEqualTo(0.8);
        assertThat(dto.getTotalDistanceSum()).isEqualTo(6000.0);
    }

    @Test
    void getAllDynasties_emptyRepository_returnsEmptyList() {
        when(dynastyRouteRepository.findAll()).thenReturn(Collections.emptyList());

        List<DynastyComparisonDTO> result = routeEvolutionService.getAllDynasties();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getRoutesByDynasty_nonexistentDynasty_returnsEmptyList() {
        when(dynastyRouteRepository.findByDynasty("XIA")).thenReturn(Collections.emptyList());

        List<DynastyRouteDTO> result = routeEvolutionService.getRoutesByDynasty("XIA");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void compareDynasties_oneDynastyEmpty_showsZeroValues() {
        LineString geom = createSampleLineString();
        DynastyRoute r1 = createDynastyRoute(1L, "TANG", "唐代", 618, 907, 0.9, 0.9, 0.8, 5000.0, geom);

        when(dynastyRouteRepository.findByDynasty("TANG")).thenReturn(Collections.singletonList(r1));
        when(dynastyRouteRepository.findByDynasty("XIA")).thenReturn(Collections.emptyList());

        Map<String, DynastyComparisonDTO> result = routeEvolutionService.compareDynasties("TANG", "XIA");
        DynastyComparisonDTO xiaDto = result.get("XIA");

        assertThat(xiaDto).isNotNull();
        assertThat(xiaDto.getRouteCount()).isEqualTo(0);
        assertThat(xiaDto.getAvgPoliticalStability()).isEqualTo(0.0);
        assertThat(xiaDto.getAvgTradeVolume()).isEqualTo(0.0);
        assertThat(xiaDto.getAvgCulturalExchange()).isEqualTo(0.0);
        assertThat(xiaDto.getTotalDistanceSum()).isEqualTo(0.0);
    }

    @Test
    void toDTO_nullGeom_returnsEmptyCoordinates() {
        DynastyRoute route = createDynastyRoute(1L, "TANG", "唐代", 618, 907,
                0.85, 0.92, 0.88, 6500.0, null);

        DynastyRouteDTO dto = routeEvolutionService.toDTO(route);

        assertThat(dto).isNotNull();
        assertThat(dto.getCoordinates()).isNotNull();
        assertThat(dto.getCoordinates()).isEmpty();
    }

    @Test
    void getAllDynasties_repositoryThrowsRuntimeException_propagates() {
        when(dynastyRouteRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> routeEvolutionService.getAllDynasties());
    }

    @Test
    void compareDynasties_nullDynastyA_handlesGracefully() {
        when(dynastyRouteRepository.findByDynasty(null)).thenReturn(Collections.emptyList());
        when(dynastyRouteRepository.findByDynasty("TANG")).thenReturn(Collections.emptyList());

        Map<String, DynastyComparisonDTO> result = routeEvolutionService.compareDynasties(null, "TANG");

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getRoutesByDynasty_specialCharactersInDynasty_noException() {
        when(dynastyRouteRepository.findByDynasty("汉'朝")).thenReturn(Collections.emptyList());

        List<DynastyRouteDTO> result = routeEvolutionService.getRoutesByDynasty("汉'朝");

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void toDTO_shouldIncludeArchaeologicalEvidence() {
        LineString geom = createSampleLineString();
        DynastyRoute route = createDynastyRoute(1L, "TANG", "唐代", 618, 907,
                0.85, 0.92, 0.88, 6500.0, geom);
        route.setEvidenceStrength(0.85);
        route.setRouteQuality("EXCELLENT");
        route.setNumArchaeologicalSites(15);

        DynastyRouteDTO dto = routeEvolutionService.toDTO(route);

        assertThat(dto).isNotNull();
        assertThat(dto.getEvidenceStrength()).isEqualTo(0.85);
        assertThat(dto.getRouteQuality()).isEqualTo("EXCELLENT");
        assertThat(dto.getNumArchaeologicalSites()).isEqualTo(15);
    }

    @Test
    void getArchaeologicalSitesByDynasty_shouldReturnCorrectSites() {
        ArchaeologicalSite site1 = new ArchaeologicalSite();
        site1.setId(1L);
        site1.setSiteName("西安唐城遗址");
        site1.setDynasty("TANG");
        site1.setEvidenceStrength(0.9);

        ArchaeologicalSite site2 = new ArchaeologicalSite();
        site2.setId(2L);
        site2.setSiteName("敦煌莫高窟");
        site2.setDynasty("TANG");
        site2.setEvidenceStrength(0.95);

        when(archaeologicalSiteRepository.findByDynasty("TANG")).thenReturn(Arrays.asList(site1, site2));

        List<ArchaeologicalSite> result = routeEvolutionService.getArchaeologicalSitesByDynasty("TANG");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSiteName()).isEqualTo("西安唐城遗址");
        assertThat(result.get(1).getSiteName()).isEqualTo("敦煌莫高窟");
        verify(archaeologicalSiteRepository, times(1)).findByDynasty("TANG");
    }

    @Test
    void getHistoricalSourcesByRoute_shouldReturnCorrectSources() {
        HistoricalSource source1 = new HistoricalSource();
        source1.setId(1L);
        source1.setTitle("大唐西域记");
        source1.setAuthor("玄奘");
        source1.setReliabilityScore(0.95);

        HistoricalSource source2 = new HistoricalSource();
        source2.setId(2L);
        source2.setTitle("新唐书·地理志");
        source2.setAuthor("欧阳修");
        source2.setReliabilityScore(0.9);

        when(historicalSourceRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(source1, source2));

        List<HistoricalSource> result = routeEvolutionService.getHistoricalSourcesByRoute(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("大唐西域记");
        assertThat(result.get(1).getTitle()).isEqualTo("新唐书·地理志");
    }
}
