package com.silkroad.dynasty_analyzer.service;

import com.silkroad.dto.DynastyComparisonDTO;
import com.silkroad.dto.DynastyRouteDTO;
import com.silkroad.entity.DynastyRoute;
import com.silkroad.repository.DynastyRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynastyAnalyzerService {

    private final DynastyRouteRepository dynastyRouteRepository;

    public List<DynastyComparisonDTO> getAllDynasties() {
        List<DynastyRoute> allRoutes = dynastyRouteRepository.findAll();
        Map<String, List<DynastyRoute>> groupedByDynasty = allRoutes.stream()
                .collect(Collectors.groupingBy(DynastyRoute::getDynasty));

        List<DynastyComparisonDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<DynastyRoute>> entry : groupedByDynasty.entrySet()) {
            result.add(buildComparisonDTO(entry.getKey(), entry.getValue()));
        }

        result.sort(Comparator.comparing(DynastyComparisonDTO::getStartYear, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    public List<DynastyRouteDTO> getRoutesByDynasty(String dynasty) {
        List<DynastyRoute> routes = dynastyRouteRepository.findByDynasty(dynasty);
        return routes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Map<String, DynastyComparisonDTO> compareDynasties(String dynastyA, String dynastyB) {
        Map<String, DynastyComparisonDTO> result = new LinkedHashMap<>();

        List<DynastyRoute> routesA = dynastyRouteRepository.findByDynasty(dynastyA);
        List<DynastyRoute> routesB = dynastyRouteRepository.findByDynasty(dynastyB);

        result.put(dynastyA, buildComparisonDTO(dynastyA, routesA));
        result.put(dynastyB, buildComparisonDTO(dynastyB, routesB));

        return result;
    }

    public List<DynastyRouteDTO> getDynastyTimeline() {
        List<DynastyRoute> routes = dynastyRouteRepository.findAllByOrderByStartYearAsc();
        return routes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DynastyRouteDTO toDTO(DynastyRoute entity) {
        List<double[]> coordinates = new ArrayList<>();
        if (entity.getGeom() != null) {
            LineString lineString = entity.getGeom();
            for (Coordinate coordinate : lineString.getCoordinates()) {
                coordinates.add(new double[]{coordinate.getX(), coordinate.getY()});
            }
        }

        return DynastyRouteDTO.builder()
                .id(entity.getId())
                .dynasty(entity.getDynasty())
                .dynastyName(entity.getDynastyName())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .startYear(entity.getStartYear())
                .endYear(entity.getEndYear())
                .totalDistanceKm(entity.getTotalDistanceKm())
                .mainCommodities(entity.getMainCommodities())
                .description(entity.getDescription())
                .politicalStability(entity.getPoliticalStability())
                .tradeVolumeScore(entity.getTradeVolumeScore())
                .culturalExchangeScore(entity.getCulturalExchangeScore())
                .coordinates(coordinates)
                .build();
    }

    private DynastyComparisonDTO buildComparisonDTO(String dynasty, List<DynastyRoute> routes) {
        if (routes == null || routes.isEmpty()) {
            return DynastyComparisonDTO.builder()
                    .dynasty(dynasty)
                    .routeCount(0)
                    .totalDistanceSum(0.0)
                    .avgPoliticalStability(0.0)
                    .avgTradeVolume(0.0)
                    .avgCulturalExchange(0.0)
                    .routes(Collections.emptyList())
                    .build();
        }

        DynastyRoute first = routes.get(0);
        int routeCount = routes.size();
        Double totalDistanceSum = routes.stream()
                .map(DynastyRoute::getTotalDistanceKm)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        Double avgPoliticalStability = routes.stream()
                .map(DynastyRoute::getPoliticalStability)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Double avgTradeVolume = routes.stream()
                .map(DynastyRoute::getTradeVolumeScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Double avgCulturalExchange = routes.stream()
                .map(DynastyRoute::getCulturalExchangeScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Integer minStartYear = routes.stream()
                .map(DynastyRoute::getStartYear)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);

        Integer maxEndYear = routes.stream()
                .map(DynastyRoute::getEndYear)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);

        List<DynastyRouteDTO> routeDTOs = routes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return DynastyComparisonDTO.builder()
                .dynasty(dynasty)
                .dynastyName(first.getDynastyName())
                .startYear(minStartYear)
                .endYear(maxEndYear)
                .routeCount(routeCount)
                .totalDistanceSum(totalDistanceSum)
                .avgPoliticalStability(avgPoliticalStability)
                .avgTradeVolume(avgTradeVolume)
                .avgCulturalExchange(avgCulturalExchange)
                .routes(routeDTOs)
                .build();
    }
}
