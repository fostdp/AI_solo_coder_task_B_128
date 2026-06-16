package com.silkroad.dynasty_analyzer.controller;

import com.silkroad.dto.DynastyComparisonDTO;
import com.silkroad.dto.DynastyRouteDTO;
import com.silkroad.dynasty_analyzer.service.DynastyAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dynasties")
@RequiredArgsConstructor
public class DynastyAnalyzerController {

    private final DynastyAnalyzerService dynastyAnalyzerService;

    @GetMapping
    public List<DynastyComparisonDTO> getAllDynasties() {
        return dynastyAnalyzerService.getAllDynasties();
    }

    @GetMapping("/{dynasty}")
    public List<DynastyRouteDTO> getRoutesByDynasty(@PathVariable String dynasty) {
        return dynastyAnalyzerService.getRoutesByDynasty(dynasty);
    }

    @GetMapping("/compare")
    public Map<String, DynastyComparisonDTO> compareDynasties(
            @RequestParam String dynastyA,
            @RequestParam String dynastyB) {
        return dynastyAnalyzerService.compareDynasties(dynastyA, dynastyB);
    }

    @GetMapping("/timeline")
    public List<DynastyRouteDTO> getDynastyTimeline() {
        return dynastyAnalyzerService.getDynastyTimeline();
    }
}
