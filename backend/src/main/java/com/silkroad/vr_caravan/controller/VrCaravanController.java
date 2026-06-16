package com.silkroad.vr_caravan.controller;

import com.silkroad.vr_caravan.dto.CaravanJourneyEventDTO;
import com.silkroad.vr_caravan.dto.CreateVirtualCaravanRequest;
import com.silkroad.vr_caravan.dto.VirtualCaravanDTO;
import com.silkroad.vr_caravan.service.VrCaravanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/virtual-caravans")
@RequiredArgsConstructor
@CrossOrigin
public class VrCaravanController {

    private final VrCaravanService vrCaravanService;

    @PostMapping
    public ResponseEntity<VirtualCaravanDTO> createVirtualCaravan(@RequestBody CreateVirtualCaravanRequest request) {
        VirtualCaravanDTO result = vrCaravanService.createVirtualCaravan(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VirtualCaravanDTO> getVirtualCaravan(@PathVariable Long id) {
        VirtualCaravanDTO result = vrCaravanService.getVirtualCaravan(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<VirtualCaravanDTO> getCaravanBySession(@PathVariable String sessionId) {
        VirtualCaravanDTO result = vrCaravanService.getCaravanBySession(sessionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/public")
    public ResponseEntity<List<VirtualCaravanDTO>> getPublicCaravans() {
        List<VirtualCaravanDTO> result = vrCaravanService.getPublicCaravans();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public ResponseEntity<List<VirtualCaravanDTO>> getActiveCaravans() {
        List<VirtualCaravanDTO> result = vrCaravanService.getActiveCaravans();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<VirtualCaravanDTO> startJourney(@PathVariable Long id) {
        VirtualCaravanDTO result = vrCaravanService.startJourney(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<VirtualCaravanDTO> pauseJourney(@PathVariable Long id) {
        VirtualCaravanDTO result = vrCaravanService.pauseJourney(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<VirtualCaravanDTO> resumeJourney(@PathVariable Long id) {
        VirtualCaravanDTO result = vrCaravanService.resumeJourney(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<CaravanJourneyEventDTO>> getJourneyEvents(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        List<CaravanJourneyEventDTO> result = vrCaravanService.getJourneyEvents(id, limit);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVirtualCaravan(@PathVariable Long id) {
        vrCaravanService.deleteVirtualCaravan(id);
        return ResponseEntity.ok(Map.of("message", "驼队已删除"));
    }
}
