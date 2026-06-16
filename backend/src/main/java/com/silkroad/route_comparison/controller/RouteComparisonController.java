package com.silkroad.route_comparison.controller;

import com.silkroad.dto.ModernRoadDTO;
import com.silkroad.dto.RouteComparisonDTO;
import com.silkroad.route_comparison.service.RouteComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/route-comparison")
@RequiredArgsConstructor
public class RouteComparisonController {

    private final RouteComparisonService routeComparisonService;

    @GetMapping("/modern-roads")
    public List<ModernRoadDTO> getAllModernRoads() {
        return routeComparisonService.getAllModernRoads();
    }

    @GetMapping("/modern/{modernRoadId}")
    public RouteComparisonDTO compareByModernRoadId(@PathVariable Long modernRoadId) {
        return routeComparisonService.compareByModernRoadId(modernRoadId);
    }

    @GetMapping("/ancient/{ancientRouteId}")
    public List<RouteComparisonDTO> compareByAncientRouteId(@PathVariable Long ancientRouteId) {
        return routeComparisonService.compareByAncientRouteId(ancientRouteId);
    }

    @GetMapping("/all")
    public List<RouteComparisonDTO> compareAllPairs() {
        return routeComparisonService.compareAllPairs();
    }
}
