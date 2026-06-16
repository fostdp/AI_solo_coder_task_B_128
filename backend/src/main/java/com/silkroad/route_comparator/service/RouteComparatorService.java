package com.silkroad.route_comparator.service;

import com.silkroad.route_comparator.dto.ModernRoadDTO;
import com.silkroad.route_comparator.dto.RouteComparisonDTO;
import com.silkroad.route_comparator.entity.ModernRoad;
import com.silkroad.entity.Route;
import com.silkroad.route_comparator.repository.ModernRoadRepository;
import com.silkroad.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.distance.HausdorffDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteComparatorService {

    private final ModernRoadRepository modernRoadRepository;
    private final RouteRepository routeRepository;

    private static final double ANCIENT_DAILY_SPEED_KM = 25.0;
    private static final double MODERN_SPEED_EFFICIENCY = 0.8;
    private static final double DAILY_WATER_CONSUMPTION_LITERS = 1140.0;

    public RouteComparisonDTO compareByModernRoadId(Long modernRoadId) {
        ModernRoad modernRoad = modernRoadRepository.findById(modernRoadId).orElse(null);
        if (modernRoad == null) {
            return null;
        }
        Long ancientRouteId = modernRoad.getCorrespondingAncientRouteId();
        if (ancientRouteId == null) {
            return null;
        }
        Route ancientRoute = routeRepository.findById(ancientRouteId).orElse(null);
        if (ancientRoute == null) {
            return null;
        }
        return buildComparison(ancientRoute, modernRoad);
    }

    public List<RouteComparisonDTO> compareByAncientRouteId(Long ancientRouteId) {
        Route ancientRoute = routeRepository.findById(ancientRouteId).orElse(null);
        if (ancientRoute == null) {
            return new ArrayList<>();
        }
        List<ModernRoad> modernRoads = modernRoadRepository.findByCorrespondingAncientRouteId(ancientRouteId);
        return modernRoads.stream()
                .map(road -> buildComparison(ancientRoute, road))
                .collect(Collectors.toList());
    }

    public List<ModernRoadDTO> getAllModernRoads() {
        return modernRoadRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RouteComparisonDTO> compareAllPairs() {
        List<RouteComparisonDTO> results = new ArrayList<>();
        List<ModernRoad> modernRoads = modernRoadRepository.findAll();
        for (ModernRoad modernRoad : modernRoads) {
            if (modernRoad.getCorrespondingAncientRouteId() != null) {
                Route ancientRoute = routeRepository.findById(modernRoad.getCorrespondingAncientRouteId()).orElse(null);
                if (ancientRoute != null) {
                    results.add(buildComparison(ancientRoute, modernRoad));
                }
            }
        }
        return results;
    }

    public ModernRoadDTO toDTO(ModernRoad entity) {
        List<double[]> coords = new ArrayList<>();
        if (entity.getGeom() != null) {
            LineString line = entity.getGeom();
            for (Coordinate coord : line.getCoordinates()) {
                coords.add(new double[]{coord.x, coord.y});
            }
        }

        return ModernRoadDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nameEn(entity.getNameEn())
                .roadType(entity.getRoadType())
                .totalDistanceKm(entity.getTotalDistanceKm())
                .speedLimitKmh(entity.getSpeedLimitKmh())
                .laneCount(entity.getLaneCount())
                .paved(entity.getPaved())
                .yearBuilt(entity.getYearBuilt())
                .correspondingAncientRouteId(entity.getCorrespondingAncientRouteId())
                .description(entity.getDescription())
                .coordinates(coords)
                .build();
    }

    private RouteComparisonDTO buildComparison(Route ancientRoute, ModernRoad modernRoad) {
        double ancientDistanceKm = ancientRoute.getTotalDistanceKm() != null ? ancientRoute.getTotalDistanceKm() : 0.0;
        double modernDistanceKm = modernRoad.getTotalDistanceKm() != null ? modernRoad.getTotalDistanceKm() : 0.0;

        double ancientTravelDays = ancientDistanceKm / ANCIENT_DAILY_SPEED_KM;

        double speedLimit = modernRoad.getSpeedLimitKmh() != null ? modernRoad.getSpeedLimitKmh() : 80.0;
        double modernEffectiveSpeed = speedLimit * MODERN_SPEED_EFFICIENCY;
        double modernTravelHours = modernDistanceKm / modernEffectiveSpeed;

        double ancientWaterRequired = ancientTravelDays * DAILY_WATER_CONSUMPTION_LITERS;

        double distanceDiffKm = modernDistanceKm - ancientDistanceKm;

        double timeSavedRatio = ancientTravelDays > 0 ? (ancientTravelDays * 24 - modernTravelHours) / (ancientTravelDays * 24) : 0.0;

        double overlapPct = calculateOverlapPct(ancientRoute.getGeom(), modernRoad.getGeom());

        String analysis = generateAnalysis(ancientRoute, modernRoad, ancientTravelDays, modernTravelHours, overlapPct);

        return RouteComparisonDTO.builder()
                .ancientRouteName(ancientRoute.getName())
                .modernRoadName(modernRoad.getName())
                .ancientDistanceKm(ancientDistanceKm)
                .modernDistanceKm(modernDistanceKm)
                .distanceDiffKm(distanceDiffKm)
                .ancientTravelDays(ancientTravelDays)
                .modernTravelHours(modernTravelHours)
                .ancientWaterRequiredLiters(ancientWaterRequired)
                .timeSavedRatio(timeSavedRatio)
                .overlapPct(overlapPct)
                .analysis(analysis)
                .build();
    }

    private double calculateOverlapPct(LineString ancientGeom, LineString modernGeom) {
        if (ancientGeom == null || modernGeom == null) {
            return 0.0;
        }
        HausdorffDistance hausdorff = new HausdorffDistance(ancientGeom, modernGeom);
        double averageDistance = hausdorff.getAverageDistance();
        return Math.max(0.0, 100.0 - averageDistance * 2.0);
    }

    private String generateAnalysis(Route ancientRoute, ModernRoad modernRoad,
                                     double ancientTravelDays, double modernTravelHours, double overlapPct) {
        String ancientTimeStr = formatTime(ancientTravelDays, true);
        String modernTimeStr = formatTime(modernTravelHours, false);
        int overlapInt = (int) Math.round(overlapPct);

        return String.format("现代公路将这段古代丝路的通行时间从%s缩短至%s，路线重合度约%d%%",
                ancientTimeStr, modernTimeStr, overlapInt);
    }

    private String formatTime(double value, boolean isDays) {
        if (isDays) {
            if (value >= 30) {
                int months = (int) Math.round(value / 30);
                return months + "个月";
            } else if (value >= 7) {
                int weeks = (int) Math.round(value / 7);
                return weeks + "周";
            } else {
                int days = (int) Math.round(value);
                return days + "天";
            }
        } else {
            if (value >= 24) {
                int days = (int) Math.round(value / 24);
                return days + "天";
            } else {
                int hours = (int) Math.round(value);
                return hours + "小时";
            }
        }
    }
}
