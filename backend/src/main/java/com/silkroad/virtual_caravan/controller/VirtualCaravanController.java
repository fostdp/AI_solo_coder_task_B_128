package com.silkroad.virtual_caravan.controller;

import com.silkroad.dto.CaravanJourneyEventDTO;
import com.silkroad.dto.CreateVirtualCaravanRequest;
import com.silkroad.dto.VirtualCaravanDTO;
import com.silkroad.virtual_caravan.service.VirtualCaravanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/virtual-caravans")
@RequiredArgsConstructor
@CrossOrigin
public class VirtualCaravanController {

    private final VirtualCaravanService virtualCaravanService;

    @PostMapping
    public ResponseEntity<VirtualCaravanDTO> createVirtualCaravan(@RequestBody CreateVirtualCaravanRequest request) {
        VirtualCaravanDTO result = virtualCaravanService.createVirtualCaravan(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VirtualCaravanDTO> getVirtualCaravan(@PathVariable Long id) {
        VirtualCaravanDTO result = virtualCaravanService.getVirtualCaravan(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<VirtualCaravanDTO> getCaravanBySession(@PathVariable String sessionId) {
        VirtualCaravanDTO result = virtualCaravanService.getCaravanBySession(sessionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/public")
    public ResponseEntity<List<VirtualCaravanDTO>> getPublicCaravans() {
        List<VirtualCaravanDTO> result = virtualCaravanService.getPublicCaravans();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public ResponseEntity<List<VirtualCaravanDTO>> getActiveCaravans() {
        List<VirtualCaravanDTO> result = virtualCaravanService.getActiveCaravans();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<VirtualCaravanDTO> startJourney(@PathVariable Long id) {
        VirtualCaravanDTO result = virtualCaravanService.startJourney(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<VirtualCaravanDTO> pauseJourney(@PathVariable Long id) {
        VirtualCaravanDTO result = virtualCaravanService.pauseJourney(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<VirtualCaravanDTO> resumeJourney(@PathVariable Long id) {
        VirtualCaravanDTO result = virtualCaravanService.resumeJourney(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<CaravanJourneyEventDTO>> getJourneyEvents(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        List<CaravanJourneyEventDTO> result = virtualCaravanService.getJourneyEvents(id, limit);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVirtualCaravan(@PathVariable Long id) {
        virtualCaravanService.deleteVirtualCaravan(id);
        return ResponseEntity.ok(Map.of("message", "驼队已删除"));
    }
}
