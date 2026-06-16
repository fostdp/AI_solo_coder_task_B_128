package com.silkroad.route_comparator.controller;

import com.silkroad.route_comparator.dto.ModernRoadDTO;
import com.silkroad.route_comparator.dto.RouteComparisonDTO;
import com.silkroad.route_comparator.service.RouteComparatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/route-comparison")
@RequiredArgsConstructor
public class RouteComparatorController {

    private final RouteComparatorService routeComparatorService;

    @GetMapping("/modern-roads")
    public List<ModernRoadDTO> getAllModernRoads() {
        return routeComparatorService.getAllModernRoads();
    }

    @GetMapping("/modern/{modernRoadId}")
    public RouteComparisonDTO compareByModernRoadId(@PathVariable Long modernRoadId) {
        return routeComparatorService.compareByModernRoadId(modernRoadId);
    }

    @GetMapping("/ancient/{ancientRouteId}")
    public List<RouteComparisonDTO> compareByAncientRouteId(@PathVariable Long ancientRouteId) {
        return routeComparatorService.compareByAncientRouteId(ancientRouteId);
    }

    @GetMapping("/all")
    public List<RouteComparisonDTO> compareAllPairs() {
        return routeComparatorService.compareAllPairs();
    }
}
