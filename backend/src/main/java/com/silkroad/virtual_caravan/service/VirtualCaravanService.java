package com.silkroad.virtual_caravan.service;

import com.silkroad.dto.CaravanJourneyEventDTO;
import com.silkroad.dto.CreateVirtualCaravanRequest;
import com.silkroad.dto.VirtualCaravanDTO;
import com.silkroad.entity.*;
import com.silkroad.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualCaravanService {

    private final VirtualCaravanRepository virtualCaravanRepository;
    private final WaypointRepository waypointRepository;
    private final CaravanJourneyEventRepository caravanJourneyEventRepository;
    private final JourneyEventConfigRepository journeyEventConfigRepository;
    private final CargoWaterConfigRepository cargoWaterConfigRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public static final double EARTH_RADIUS_KM = 6371.0;
    public static final double DEFAULT_ANCIENT_DAILY_DISTANCE_KM = 25.0;
    public static final int SIMULATION_TICK_MS = 3000;
    public static final int SIMULATED_DAYS_PER_TICK = 1;

    @Transactional
    public VirtualCaravanDTO createVirtualCaravan(CreateVirtualCaravanRequest req) {
        VirtualCaravan caravan = new VirtualCaravan();

        String sessionId = req.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        caravan.setSessionId(sessionId);

        caravan.setName(req.getName() != null ? req.getName() : "丝路驼队");
        caravan.setLeaderName(req.getLeaderName());
        caravan.setRouteId(req.getRouteId());

        List<Waypoint> waypoints = waypointRepository.findByRouteIdOrderByWaypointOrderAsc(req.getRouteId());
        if (waypoints.size() < 2) {
            throw new IllegalArgumentException("路线航点数量不足，至少需要2个航点");
        }

        Waypoint startWp = waypoints.get(0);
        Waypoint endWp = waypoints.get(waypoints.size() - 1);

        caravan.setStartWaypointId(startWp.getId());
        caravan.setEndWaypointId(endWp.getId());
        caravan.setCurrentPosition(startWp.getGeom());
        caravan.setCurrentWaypointId(startWp.getId());

        caravan.setCargoType(req.getCargoType());
        caravan.setCargoWeightKg(req.getCargoWeightKg() != null ? req.getCargoWeightKg() : 0.0);
        caravan.setCamelCount(req.getCamelCount() != null ? req.getCamelCount() : 10);
        caravan.setCrewCount(req.getCrewCount() != null ? req.getCrewCount() : 5);
        caravan.setSeason(req.getSeason() != null ? req.getSeason() : "SPRING");
        caravan.setIsPublic(req.getIsPublic() != null ? req.getIsPublic() : false);

        int camelCount = caravan.getCamelCount();
        int crewCount = caravan.getCrewCount();
        double baseWater = camelCount * 300.0 + crewCount * 200.0;
        double roundedWater = Math.ceil(baseWater / 500.0) * 500.0;

        Optional<CargoWaterConfig> waterConfigOpt = cargoWaterConfigRepository.findByCargoType(
                req.getCargoType() != null ? req.getCargoType() : "SILK"
        );

        caravan.setWaterCapacityLiters(roundedWater);
        caravan.setWaterSupplyLiters(roundedWater);
        caravan.setFoodSupplyDays(30.0);
        caravan.setMorale(80.0);
        caravan.setGoldCoins(100);
        caravan.setDistanceTraveledKm(0.0);
        caravan.setJourneyDaysElapsed(0);
        caravan.setProgressPct(0.0);
        caravan.setSpeedKmh(DEFAULT_ANCIENT_DAILY_DISTANCE_KM / 24.0);
        caravan.setStatus("PREPARING");
        caravan.setLastActiveAt(LocalDateTime.now());

        VirtualCaravan saved = virtualCaravanRepository.save(caravan);

        CaravanJourneyEvent startEvent = new CaravanJourneyEvent();
        startEvent.setVirtualCaravanId(saved.getId());
        startEvent.setEventType("JOURNEY_START");
        startEvent.setSeverity("INFO");
        startEvent.setTitle("驼队整装待发");
        startEvent.setMessage(saved.getLeaderName() != null
                ? saved.getLeaderName() + "率领驼队在" + startWp.getName() + "集结完毕，准备启程"
                : "驼队在" + startWp.getName() + "集结完毕，准备启程");
        startEvent.setGeom(saved.getCurrentPosition());
        startEvent.setEffectWaterLiters(0.0);
        startEvent.setEffectFoodDays(0.0);
        startEvent.setEffectMorale(0.0);
        startEvent.setEffectGoldCoins(0);
        startEvent.setIsResolved(true);
        startEvent.setEventTime(LocalDateTime.now());
        caravanJourneyEventRepository.save(startEvent);

        return toDTO(saved);
    }

    @Transactional
    public VirtualCaravanDTO startJourney(Long caravanId) {
        VirtualCaravan caravan = virtualCaravanRepository.findById(caravanId)
                .orElseThrow(() -> new IllegalArgumentException("驼队不存在: " + caravanId));

        if (!"PREPARING".equals(caravan.getStatus())) {
            throw new IllegalStateException("驼队当前状态不允许出发");
        }

        caravan.setStatus("TRAVELING");
        caravan.setStartedAt(LocalDateTime.now());
        caravan.setLastActiveAt(LocalDateTime.now());

        VirtualCaravan saved = virtualCaravanRepository.save(caravan);

        List<Waypoint> waypoints = waypointRepository.findByRouteIdOrderByWaypointOrderAsc(caravan.getRouteId());
        Waypoint startWp = waypoints.get(0);

        CaravanJourneyEvent event = new CaravanJourneyEvent();
        event.setVirtualCaravanId(saved.getId());
        event.setEventType("DEPARTURE");
        event.setSeverity("INFO");
        event.setTitle("启程出发");
        event.setMessage("驼队从" + startWp.getName() + "正式出发，踏上丝绸之路之旅");
        event.setGeom(saved.getCurrentPosition());
        event.setEffectWaterLiters(0.0);
        event.setEffectFoodDays(0.0);
        event.setEffectMorale(5.0);
        event.setEffectGoldCoins(0);
        event.setIsResolved(true);
        event.setEventTime(LocalDateTime.now());
        caravanJourneyEventRepository.save(event);

        broadcastStatus(saved);

        return toDTO(saved);
    }

    @Transactional
    public VirtualCaravanDTO pauseJourney(Long caravanId) {
        VirtualCaravan caravan = virtualCaravanRepository.findById(caravanId)
                .orElseThrow(() -> new IllegalArgumentException("驼队不存在: " + caravanId));

        if (!"TRAVELING".equals(caravan.getStatus())) {
            throw new IllegalStateException("驼队当前状态不允许暂停");
        }

        caravan.setStatus("RESTING");
        caravan.setLastActiveAt(LocalDateTime.now());
        VirtualCaravan saved = virtualCaravanRepository.save(caravan);
        broadcastStatus(saved);
        return toDTO(saved);
    }

    @Transactional
    public VirtualCaravanDTO resumeJourney(Long caravanId) {
        VirtualCaravan caravan = virtualCaravanRepository.findById(caravanId)
                .orElseThrow(() -> new IllegalArgumentException("驼队不存在: " + caravanId));

        if (!"RESTING".equals(caravan.getStatus())) {
            throw new IllegalStateException("驼队当前状态不允许继续");
        }

        caravan.setStatus("TRAVELING");
        caravan.setLastActiveAt(LocalDateTime.now());
        VirtualCaravan saved = virtualCaravanRepository.save(caravan);
        broadcastStatus(saved);
        return toDTO(saved);
    }

    public VirtualCaravanDTO getVirtualCaravan(Long id) {
        VirtualCaravan caravan = virtualCaravanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("驼队不存在: " + id));
        return toDTO(caravan);
    }

    public VirtualCaravanDTO getCaravanBySession(String sessionId) {
        List<VirtualCaravan> caravans = virtualCaravanRepository.findBySessionId(sessionId);
        if (caravans.isEmpty()) {
            return null;
        }
        caravans.sort(Comparator.comparing(VirtualCaravan::getCreatedAt).reversed());
        return toDTO(caravans.get(0));
    }

    public List<VirtualCaravanDTO> getPublicCaravans() {
        List<VirtualCaravan> caravans = virtualCaravanRepository.findByIsPublicTrue();
        List<VirtualCaravanDTO> result = new ArrayList<>();
        for (VirtualCaravan c : caravans) {
            result.add(toDTO(c));
        }
        return result;
    }

    public List<CaravanJourneyEventDTO> getJourneyEvents(Long caravanId, Integer limit) {
        List<CaravanJourneyEvent> events;
        if (limit != null && limit > 0) {
            events = caravanJourneyEventRepository.findTop20ByVirtualCaravanIdOrderByEventTimeDesc(caravanId);
            if (events.size() > limit) {
                events = events.subList(0, limit);
            }
        } else {
            events = caravanJourneyEventRepository.findByVirtualCaravanIdOrderByEventTimeDesc(caravanId);
        }
        List<CaravanJourneyEventDTO> result = new ArrayList<>();
        for (CaravanJourneyEvent e : events) {
            result.add(eventToDTO(e));
        }
        return result;
    }

    public List<VirtualCaravanDTO> getActiveCaravans() {
        List<VirtualCaravan> caravans = virtualCaravanRepository.findByStatus("TRAVELING");
        List<VirtualCaravanDTO> result = new ArrayList<>();
        for (VirtualCaravan c : caravans) {
            result.add(toDTO(c));
        }
        return result;
    }

    @Transactional
    public void deleteVirtualCaravan(Long caravanId) {
        if (!virtualCaravanRepository.existsById(caravanId)) {
            throw new IllegalArgumentException("驼队不存在: " + caravanId);
        }
        virtualCaravanRepository.deleteById(caravanId);
    }

    @Scheduled(fixedRate = SIMULATION_TICK_MS)
    @Transactional
    public void simulateTick() {
        List<VirtualCaravan> travelingCaravans = virtualCaravanRepository.findByStatus("TRAVELING");
        if (travelingCaravans.isEmpty()) {
            return;
        }

        for (VirtualCaravan caravan : travelingCaravans) {
            try {
                simulateCaravanTick(caravan);
            } catch (Exception e) {
                log.error("模拟驼队 {} 时出错: {}", caravan.getId(), e.getMessage(), e);
            }
        }
    }

    private void simulateCaravanTick(VirtualCaravan caravan) {
        List<Waypoint> waypoints = waypointRepository.findByRouteIdOrderByWaypointOrderAsc(caravan.getRouteId());
        if (waypoints.size() < 2) {
            return;
        }

        Waypoint currentWp = findCurrentWaypoint(caravan, waypoints);
        Waypoint nextWp = findNextWaypoint(caravan, waypoints);

        if (nextWp == null) {
            completeJourney(caravan, waypoints.get(waypoints.size() - 1));
            return;
        }

        double currentLng = caravan.getCurrentPosition().getX();
        double currentLat = caravan.getCurrentPosition().getY();
        double targetLng = nextWp.getGeom().getX();
        double targetLat = nextWp.getGeom().getY();

        double distanceToNext = haversineDistance(currentLng, currentLat, targetLng, targetLat);
        double moveDistance = SIMULATED_DAYS_PER_TICK * DEFAULT_ANCIENT_DAILY_DISTANCE_KM;

        double totalDistance = calculateTotalRouteDistance(waypoints);

        boolean reachedWaypoint = false;
        if (distanceToNext <= moveDistance) {
            caravan.setCurrentPosition(nextWp.getGeom());
            caravan.setCurrentWaypointId(nextWp.getId());
            caravan.setDistanceTraveledKm(caravan.getDistanceTraveledKm() + distanceToNext);
            reachedWaypoint = true;
        } else {
            double ratio = moveDistance / distanceToNext;
            double newLng = currentLng + (targetLng - currentLng) * ratio;
            double newLat = currentLat + (targetLat - currentLat) * ratio;
            caravan.setCurrentPosition(geometryFactory.createPoint(new Coordinate(newLng, newLat)));
            caravan.setDistanceTraveledKm(caravan.getDistanceTraveledKm() + moveDistance);
        }

        if (totalDistance > 0) {
            double progress = Math.min(100.0, (caravan.getDistanceTraveledKm() / totalDistance) * 100.0);
            caravan.setProgressPct(progress);
        }

        for (int i = 0; i < SIMULATED_DAYS_PER_TICK; i++) {
            consumeDailySupplies(caravan, currentWp);
        }

        caravan.setJourneyDaysElapsed(caravan.getJourneyDaysElapsed() + SIMULATED_DAYS_PER_TICK);
        caravan.setLastActiveAt(LocalDateTime.now());

        if (reachedWaypoint) {
            handleWaypointArrival(caravan, nextWp);
            if (nextWp.getId().equals(caravan.getEndWaypointId())) {
                completeJourney(caravan, nextWp);
                virtualCaravanRepository.save(caravan);
                broadcastStatus(caravan);
                return;
            }
        }

        if (caravan.getWaterSupplyLiters() < 0 || caravan.getFoodSupplyDays() < 0
                || (caravan.getMorale() != null && caravan.getMorale() < 20)) {
            strandCaravan(caravan);
            virtualCaravanRepository.save(caravan);
            broadcastStatus(caravan);
            return;
        }

        Random random = new Random();
        if (random.nextDouble() < 0.15) {
            triggerRandomEvent(caravan, currentWp);
        }

        virtualCaravanRepository.save(caravan);
        broadcastStatus(caravan);
    }

    private Waypoint findCurrentWaypoint(VirtualCaravan caravan, List<Waypoint> waypoints) {
        Long currentWpId = caravan.getCurrentWaypointId();
        for (Waypoint wp : waypoints) {
            if (wp.getId().equals(currentWpId)) {
                return wp;
            }
        }
        return waypoints.get(0);
    }

    private Waypoint findNextWaypoint(VirtualCaravan caravan, List<Waypoint> waypoints) {
        Long currentWpId = caravan.getCurrentWaypointId();
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).getId().equals(currentWpId)) {
                if (i < waypoints.size() - 1) {
                    return waypoints.get(i + 1);
                }
                break;
            }
        }
        return null;
    }

    private double calculateTotalRouteDistance(List<Waypoint> waypoints) {
        double total = 0.0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint a = waypoints.get(i);
            Waypoint b = waypoints.get(i + 1);
            total += haversineDistance(
                    a.getGeom().getX(), a.getGeom().getY(),
                    b.getGeom().getX(), b.getGeom().getY()
            );
        }
        return total;
    }

    private void handleWaypointArrival(VirtualCaravan caravan, Waypoint wp) {
        boolean hasWater = (wp.getIsOasis() != null && wp.getIsOasis())
                || (wp.getWaterAvailable() != null && wp.getWaterAvailable());

        if (hasWater && caravan.getWaterCapacityLiters() != null) {
            caravan.setWaterSupplyLiters(caravan.getWaterCapacityLiters() * 0.9);
        }

        if (wp.getSupplyStation() != null && wp.getSupplyStation()) {
            caravan.setFoodSupplyDays(30.0);
        }

        CaravanJourneyEvent event = new CaravanJourneyEvent();
        event.setVirtualCaravanId(caravan.getId());
        event.setEventType("WAYPOINT_ARRIVAL");
        event.setSeverity("INFO");
        event.setTitle("到达" + wp.getName());
        StringBuilder msg = new StringBuilder("驼队抵达" + wp.getName());
        if (hasWater) {
            msg.append("，补充了充足的水源");
        }
        if (wp.getSupplyStation() != null && wp.getSupplyStation()) {
            msg.append("，补给了食物和物资");
        }
        event.setMessage(msg.toString());
        event.setGeom(wp.getGeom());
        event.setEffectWaterLiters(0.0);
        event.setEffectFoodDays(0.0);
        event.setEffectMorale(5.0);
        event.setEffectGoldCoins(0);
        event.setIsResolved(true);
        event.setEventTime(LocalDateTime.now());
        caravanJourneyEventRepository.save(event);

        broadcastEvent(caravan.getId(), eventToDTO(event));
    }

    private void completeJourney(VirtualCaravan caravan, Waypoint endWp) {
        caravan.setStatus("COMPLETED");
        caravan.setProgressPct(100.0);
        caravan.setCurrentPosition(endWp.getGeom());
        caravan.setCurrentWaypointId(endWp.getId());

        int bonusGold = 500 + (caravan.getMorale() != null ? caravan.getMorale().intValue() : 0) * 5;
        caravan.setGoldCoins((caravan.getGoldCoins() != null ? caravan.getGoldCoins() : 0) + bonusGold);

        CaravanJourneyEvent event = new CaravanJourneyEvent();
        event.setVirtualCaravanId(caravan.getId());
        event.setEventType("JOURNEY_COMPLETE");
        event.setSeverity("SUCCESS");
        event.setTitle("旅程完成");
        event.setMessage("驼队成功抵达目的地" + endWp.getName() + "！获得" + bonusGold + "金币奖励");
        event.setGeom(endWp.getGeom());
        event.setEffectWaterLiters(0.0);
        event.setEffectFoodDays(0.0);
        event.setEffectMorale(20.0);
        event.setEffectGoldCoins(bonusGold);
        event.setIsResolved(true);
        event.setEventTime(LocalDateTime.now());
        caravanJourneyEventRepository.save(event);

        broadcastEvent(caravan.getId(), eventToDTO(event));
    }

    private void strandCaravan(VirtualCaravan caravan) {
        caravan.setStatus("STRANDED");

        String reason;
        if (caravan.getWaterSupplyLiters() < 0) {
            reason = "水源耗尽";
        } else if (caravan.getFoodSupplyDays() < 0) {
            reason = "食物告罄";
        } else {
            reason = "士气崩溃";
        }

        CaravanJourneyEvent event = new CaravanJourneyEvent();
        event.setVirtualCaravanId(caravan.getId());
        event.setEventType("STRANDED");
        event.setSeverity("DANGER");
        event.setTitle("驼队遇险");
        event.setMessage("驼队因" + reason + "在大漠中迷失，请求支援！");
        event.setGeom(caravan.getCurrentPosition());
        event.setEffectWaterLiters(0.0);
        event.setEffectFoodDays(0.0);
        event.setEffectMorale(-10.0);
        event.setEffectGoldCoins(0);
        event.setIsResolved(false);
        event.setEventTime(LocalDateTime.now());
        caravanJourneyEventRepository.save(event);

        broadcastEvent(caravan.getId(), eventToDTO(event));
    }

    public CaravanJourneyEventDTO triggerRandomEvent(VirtualCaravan caravan, Waypoint currentWp) {
        List<JourneyEventConfig> allConfigs = journeyEventConfigRepository.findAll();
        if (allConfigs.isEmpty()) {
            return null;
        }

        String terrainType = inferTerrainType(currentWp);
        List<JourneyEventConfig> matchingConfigs = new ArrayList<>();

        for (JourneyEventConfig cfg : allConfigs) {
            if (cfg.getTerrainTypes() == null || cfg.getTerrainTypes().isEmpty()) {
                matchingConfigs.add(cfg);
            } else {
                String[] types = cfg.getTerrainTypes().split(",");
                for (String t : types) {
                    if (t.trim().equalsIgnoreCase(terrainType) || t.trim().equalsIgnoreCase("ALL")) {
                        matchingConfigs.add(cfg);
                        break;
                    }
                }
            }
        }

        if (matchingConfigs.isEmpty()) {
            matchingConfigs = allConfigs;
        }

        double totalWeight = 0.0;
        List<Double> weights = new ArrayList<>();
        for (JourneyEventConfig cfg : matchingConfigs) {
            double minP = cfg.getMinOccurrenceProb() != null ? cfg.getMinOccurrenceProb() : 0.1;
            double maxP = cfg.getMaxOccurrenceProb() != null ? cfg.getMaxOccurrenceProb() : 0.5;
            double p = (minP + maxP) / 2.0;
            weights.add(p);
            totalWeight += p;
        }

        Random random = new Random();
        double r = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        JourneyEventConfig selectedConfig = matchingConfigs.get(0);

        for (int i = 0; i < matchingConfigs.size(); i++) {
            cumulative += weights.get(i);
            if (r <= cumulative) {
                selectedConfig = matchingConfigs.get(i);
                break;
            }
        }

        double waterEffect = randomEffect(
                selectedConfig.getWaterEffectMin(),
                selectedConfig.getWaterEffectMax()
        );
        double foodEffect = randomEffect(
                selectedConfig.getFoodEffectMin(),
                selectedConfig.getFoodEffectMax()
        );
        double moraleEffect = randomEffect(
                selectedConfig.getMoraleEffectMin(),
                selectedConfig.getMoraleEffectMax()
        );
        int goldEffect = (int) randomEffect(
                selectedConfig.getGoldEffectMin() != null ? selectedConfig.getGoldEffectMin().doubleValue() : null,
                selectedConfig.getGoldEffectMax() != null ? selectedConfig.getGoldEffectMax().doubleValue() : null
        );

        if (caravan.getWaterSupplyLiters() != null) {
            caravan.setWaterSupplyLiters(Math.max(0.0, caravan.getWaterSupplyLiters() + waterEffect));
        }
        if (caravan.getFoodSupplyDays() != null) {
            caravan.setFoodSupplyDays(Math.max(0.0, caravan.getFoodSupplyDays() + foodEffect));
        }
        if (caravan.getMorale() != null) {
            caravan.setMorale(Math.max(0.0, Math.min(100.0, caravan.getMorale() + moraleEffect)));
        }
        if (caravan.getGoldCoins() != null) {
            caravan.setGoldCoins(Math.max(0, caravan.getGoldCoins() + goldEffect));
        }

        CaravanJourneyEvent event = new CaravanJourneyEvent();
        event.setVirtualCaravanId(caravan.getId());
        event.setEventType(selectedConfig.getEventType());
        event.setSeverity(selectedConfig.getSeverity() != null ? selectedConfig.getSeverity() : "INFO");
        event.setTitle(selectedConfig.getEventName() != null ? selectedConfig.getEventName() : "旅途中的事件");
        event.setMessage(selectedConfig.getDescription() != null ? selectedConfig.getDescription() : "");
        event.setGeom(caravan.getCurrentPosition());
        event.setEffectWaterLiters(waterEffect);
        event.setEffectFoodDays(foodEffect);
        event.setEffectMorale(moraleEffect);
        event.setEffectGoldCoins(goldEffect);
        event.setIsResolved(true);
        event.setEventTime(LocalDateTime.now());
        caravanJourneyEventRepository.save(event);

        CaravanJourneyEventDTO dto = eventToDTO(event);
        broadcastEvent(caravan.getId(), dto);
        return dto;
    }

    private double randomEffect(Double min, Double max) {
        if (min == null && max == null) {
            return 0.0;
        }
        if (min == null) {
            min = 0.0;
        }
        if (max == null) {
            max = min;
        }
        if (min.equals(max)) {
            return min;
        }
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    private String inferTerrainType(Waypoint wp) {
        if (wp == null) {
            return "DESERT";
        }
        if (wp.getIsOasis() != null && wp.getIsOasis()) {
            return "OASIS";
        }
        if (wp.getElevationM() != null && wp.getElevationM() > 1500) {
            return "MOUNTAINS";
        }
        return "DESERT";
    }

    private void consumeDailySupplies(VirtualCaravan caravan, Waypoint waypoint) {
        String terrainType = inferTerrainType(waypoint);
        String season = caravan.getSeason() != null ? caravan.getSeason() : "SPRING";

        double waterConsumption = calculateWaterConsumptionPerDay(caravan, terrainType, season);
        double foodConsumptionPerCrew = 1.0;
        double crewCount = caravan.getCrewCount() != null ? caravan.getCrewCount() : 0;
        double camelCount = caravan.getCamelCount() != null ? caravan.getCamelCount() : 0;
        double foodConsumption = crewCount * foodConsumptionPerCrew * 0.3 + camelCount * 0.1;

        if (caravan.getWaterSupplyLiters() != null) {
            caravan.setWaterSupplyLiters(caravan.getWaterSupplyLiters() - waterConsumption);
        }
        if (caravan.getFoodSupplyDays() != null) {
            caravan.setFoodSupplyDays(caravan.getFoodSupplyDays() - foodConsumption);
        }

        double moraleChange = 0.0;
        if (caravan.getWaterSupplyLiters() != null && caravan.getWaterCapacityLiters() != null
                && caravan.getWaterCapacityLiters() > 0) {
            double waterRatio = caravan.getWaterSupplyLiters() / caravan.getWaterCapacityLiters();
            if (waterRatio < 0.2) {
                moraleChange -= 2.0;
            }
        }
        if (caravan.getFoodSupplyDays() != null && caravan.getFoodSupplyDays() < 5) {
            moraleChange -= 1.5;
        }
        if ("RESTING".equals(caravan.getStatus())) {
            moraleChange += 1.0;
        }

        if (caravan.getMorale() != null) {
            double newMorale = Math.max(0.0, Math.min(100.0, caravan.getMorale() + moraleChange));
            caravan.setMorale(newMorale);
        }
    }

    private double calculateWaterConsumptionPerDay(VirtualCaravan caravan, String terrainType, String season) {
        String cargoType = caravan.getCargoType() != null ? caravan.getCargoType() : "SILK";
        CargoWaterConfig config = cargoWaterConfigRepository.findByCargoType(cargoType).orElse(null);

        double camelWater = config != null && config.getCamelBaseWaterDailyL() != null
                ? config.getCamelBaseWaterDailyL() : 40.0;
        double crewWater = config != null && config.getCrewBaseWaterDailyL() != null
                ? config.getCrewBaseWaterDailyL() : 8.0;

        int camelCount = caravan.getCamelCount() != null ? caravan.getCamelCount() : 0;
        int crewCount = caravan.getCrewCount() != null ? caravan.getCrewCount() : 0;

        double baseConsumption = camelCount * camelWater + crewCount * crewWater;

        double terrainFactor = 1.0;
        if (config != null) {
            if ("DESERT".equalsIgnoreCase(terrainType) && config.getTerrainFactorDesert() != null) {
                terrainFactor = config.getTerrainFactorDesert();
            } else if ("MOUNTAINS".equalsIgnoreCase(terrainType) && config.getTerrainFactorMountains() != null) {
                terrainFactor = config.getTerrainFactorMountains();
            } else if ("OASIS".equalsIgnoreCase(terrainType) && config.getTerrainFactorOasis() != null) {
                terrainFactor = config.getTerrainFactorOasis();
            }
        }

        double seasonFactor = 1.0;
        if ("SUMMER".equalsIgnoreCase(season)) {
            seasonFactor = 1.3;
        } else if ("WINTER".equalsIgnoreCase(season)) {
            seasonFactor = 0.8;
        }

        return baseConsumption * terrainFactor * seasonFactor;
    }

    public double haversineDistance(double lng1, double lat1, double lng2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public VirtualCaravanDTO toDTO(VirtualCaravan caravan) {
        VirtualCaravanDTO.VirtualCaravanDTOBuilder builder = VirtualCaravanDTO.builder();
        builder.id(caravan.getId())
                .sessionId(caravan.getSessionId())
                .name(caravan.getName())
                .leaderName(caravan.getLeaderName())
                .routeId(caravan.getRouteId())
                .progressPct(caravan.getProgressPct())
                .status(caravan.getStatus())
                .speedKmh(caravan.getSpeedKmh())
                .cargoType(caravan.getCargoType())
                .cargoWeightKg(caravan.getCargoWeightKg())
                .camelCount(caravan.getCamelCount())
                .crewCount(caravan.getCrewCount())
                .waterSupplyLiters(caravan.getWaterSupplyLiters())
                .waterCapacityLiters(caravan.getWaterCapacityLiters())
                .foodSupplyDays(caravan.getFoodSupplyDays())
                .morale(caravan.getMorale())
                .goldCoins(caravan.getGoldCoins())
                .distanceTraveledKm(caravan.getDistanceTraveledKm())
                .journeyDaysElapsed(caravan.getJourneyDaysElapsed())
                .season(caravan.getSeason())
                .isPublic(caravan.getIsPublic())
                .startedAt(caravan.getStartedAt())
                .lastActiveAt(caravan.getLastActiveAt());

        if (caravan.getCurrentPosition() != null) {
            builder.lng(caravan.getCurrentPosition().getX());
            builder.lat(caravan.getCurrentPosition().getY());
        }

        return builder.build();
    }

    public CaravanJourneyEventDTO eventToDTO(CaravanJourneyEvent event) {
        CaravanJourneyEventDTO.CaravanJourneyEventDTOBuilder builder = CaravanJourneyEventDTO.builder();
        builder.id(event.getId())
                .virtualCaravanId(event.getVirtualCaravanId())
                .eventType(event.getEventType())
                .severity(event.getSeverity())
                .title(event.getTitle())
                .message(event.getMessage())
                .effectWaterLiters(event.getEffectWaterLiters())
                .effectFoodDays(event.getEffectFoodDays())
                .effectMorale(event.getEffectMorale())
                .effectGoldCoins(event.getEffectGoldCoins())
                .isResolved(event.getIsResolved())
                .eventTime(event.getEventTime());

        if (event.getGeom() != null) {
            builder.lng(event.getGeom().getX());
            builder.lat(event.getGeom().getY());
        }

        return builder.build();
    }

    private void broadcastStatus(VirtualCaravan caravan) {
        VirtualCaravanDTO dto = toDTO(caravan);
        try {
            messagingTemplate.convertAndSend("/topic/virtual-caravans/" + caravan.getId() + "/status", dto);
            messagingTemplate.convertAndSend("/topic/virtual-caravans", dto);
        } catch (Exception e) {
            log.warn("WebSocket 广播状态失败: {}", e.getMessage());
        }
    }

    private void broadcastEvent(Long caravanId, CaravanJourneyEventDTO event) {
        try {
            messagingTemplate.convertAndSend("/topic/virtual-caravans/" + caravanId + "/events", event);
        } catch (Exception e) {
            log.warn("WebSocket 广播事件失败: {}", e.getMessage());
        }
    }
}
