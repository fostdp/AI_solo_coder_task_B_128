package com.silkroad.route_evolution.controller;

import com.silkroad.route_evolution.dto.DynastyComparisonDTO;
import com.silkroad.route_evolution.dto.DynastyRouteDTO;
import com.silkroad.route_evolution.service.RouteEvolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dynasties")
@RequiredArgsConstructor
public class RouteEvolutionController {

    private final RouteEvolutionService routeEvolutionService;

    @GetMapping
    public List<DynastyComparisonDTO> getAllDynasties() {
        return routeEvolutionService.getAllDynasties();
    }

    @GetMapping("/{dynasty}")
    public List<DynastyRouteDTO> getRoutesByDynasty(@PathVariable String dynasty) {
        return routeEvolutionService.getRoutesByDynasty(dynasty);
    }

    @GetMapping("/compare")
    public Map<String, DynastyComparisonDTO> compareDynasties(
            @RequestParam String dynastyA,
            @RequestParam String dynastyB) {
        return routeEvolutionService.compareDynasties(dynastyA, dynastyB);
    }

    @GetMapping("/timeline")
    public List<DynastyRouteDTO> getDynastyTimeline() {
        return routeEvolutionService.getDynastyTimeline();
    }
}
