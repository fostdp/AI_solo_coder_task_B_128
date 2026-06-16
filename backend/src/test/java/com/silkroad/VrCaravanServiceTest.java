package com.silkroad;

import com.silkroad.vr_caravan.dto.CaravanJourneyEventDTO;
import com.silkroad.vr_caravan.dto.CreateVirtualCaravanRequest;
import com.silkroad.vr_caravan.dto.VirtualCaravanDTO;
import com.silkroad.vr_caravan.entity.*;
import com.silkroad.vr_caravan.repository.*;
import com.silkroad.entity.Waypoint;
import com.silkroad.repository.WaypointRepository;
import com.silkroad.load_optimizer.entity.CargoWaterConfig;
import com.silkroad.load_optimizer.repository.CargoWaterConfigRepository;
import com.silkroad.vr_caravan.service.VrCaravanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VrCaravanServiceTest {

    @Mock
    private VirtualCaravanRepository virtualCaravanRepository;

    @Mock
    private WaypointRepository waypointRepository;

    @Mock
    private CaravanJourneyEventRepository caravanJourneyEventRepository;

    @Mock
    private JourneyEventConfigRepository journeyEventConfigRepository;

    @Mock
    private CargoWaterConfigRepository cargoWaterConfigRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private VrCaravanService vrCaravanService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private Waypoint createWaypoint(Long id, Long routeId, String name, double lng, double lat,
                                     int order, Boolean isOasis, Double elevationM) {
        Waypoint wp = new Waypoint();
        wp.setId(id);
        wp.setRouteId(routeId);
        wp.setName(name);
        Point geom = geometryFactory.createPoint(new Coordinate(lng, lat));
        wp.setGeom(geom);
        wp.setWaypointOrder(order);
        wp.setIsOasis(isOasis);
        wp.setElevationM(elevationM);
        return wp;
    }

    private List<Waypoint> createStandardWaypoints() {
        Waypoint wp1 = createWaypoint(1L, 1L, "西安", 108.94, 34.26, 1, true, 800.0);
        Waypoint wp2 = createWaypoint(2L, 1L, "天水", 103.83, 36.06, 2, false, 1100.0);
        return Arrays.asList(wp1, wp2);
    }

    private CargoWaterConfig createStandardWaterConfig() {
        CargoWaterConfig config = new CargoWaterConfig();
        config.setCargoType("SILK");
        config.setCargoName("丝绸");
        config.setWaterPer100kgPerDayLiters(1.5);
        config.setCamelBaseWaterDailyL(30.0);
        config.setCrewBaseWaterDailyL(12.0);
        config.setTerrainFactorDesert(1.8);
        config.setTerrainFactorMountains(1.5);
        config.setTerrainFactorOasis(0.8);
        config.setTemperatureFactorPerDegree(0.03);
        config.setMaxCargoPerCamelKg(200.0);
        config.setOptimalCargoPerCamelKg(150.0);
        return config;
    }

    private CreateVirtualCaravanRequest createStandardRequest() {
        return CreateVirtualCaravanRequest.builder()
                .sessionId("test-session-123")
                .name("测试驼队")
                .leaderName("张三")
                .routeId(1L)
                .cargoType("SILK")
                .cargoWeightKg(1500.0)
                .camelCount(10)
                .crewCount(5)
                .season("SPRING")
                .isPublic(false)
                .build();
    }

    @Test
    void createVirtualCaravan_validRequest_setsStatusPreparing() {
        CreateVirtualCaravanRequest req = createStandardRequest();
        List<Waypoint> waypoints = createStandardWaypoints();
        CargoWaterConfig waterConfig = createStandardWaterConfig();

        when(waypointRepository.findByRouteIdOrderByWaypointOrderAsc(1L)).thenReturn(waypoints);
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(waterConfig));
        when(virtualCaravanRepository.save(any(VirtualCaravan.class))).thenAnswer(inv -> {
            VirtualCaravan c = inv.getArgument(0);
            c.setId(100L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });
        when(caravanJourneyEventRepository.save(any(CaravanJourneyEvent.class))).thenAnswer(inv -> {
            CaravanJourneyEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        VirtualCaravanDTO result = vrCaravanService.createVirtualCaravan(req);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PREPARING");
        assertThat(result.getProgressPct()).isEqualTo(0.0);
        assertThat(result.getLng()).isEqualTo(108.94);
        assertThat(result.getLat()).isEqualTo(34.26);
    }

    @Test
    void createVirtualCaravan_sessionIdNull_generatesUUID() {
        CreateVirtualCaravanRequest req = createStandardRequest();
        req.setSessionId(null);
        List<Waypoint> waypoints = createStandardWaypoints();
        CargoWaterConfig waterConfig = createStandardWaterConfig();

        when(waypointRepository.findByRouteIdOrderByWaypointOrderAsc(1L)).thenReturn(waypoints);
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(waterConfig));
        when(virtualCaravanRepository.save(any(VirtualCaravan.class))).thenAnswer(inv -> {
            VirtualCaravan c = inv.getArgument(0);
            c.setId(100L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });
        when(caravanJourneyEventRepository.save(any(CaravanJourneyEvent.class))).thenAnswer(inv -> {
            CaravanJourneyEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        VirtualCaravanDTO result = vrCaravanService.createVirtualCaravan(req);

        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isNotNull();
        assertThat(result.getSessionId()).hasSize(36);
    }

    @Test
    void createVirtualCaravan_shouldCreateJourneyStartEvent() {
        CreateVirtualCaravanRequest req = createStandardRequest();
        List<Waypoint> waypoints = createStandardWaypoints();
        CargoWaterConfig waterConfig = createStandardWaterConfig();

        when(waypointRepository.findByRouteIdOrderByWaypointOrderAsc(1L)).thenReturn(waypoints);
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(waterConfig));
        when(virtualCaravanRepository.save(any(VirtualCaravan.class))).thenAnswer(inv -> {
            VirtualCaravan c = inv.getArgument(0);
            c.setId(100L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });
        when(caravanJourneyEventRepository.save(any(CaravanJourneyEvent.class))).thenAnswer(inv -> {
            CaravanJourneyEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        vrCaravanService.createVirtualCaravan(req);

        ArgumentCaptor<CaravanJourneyEvent> eventCaptor = ArgumentCaptor.forClass(CaravanJourneyEvent.class);
        verify(caravanJourneyEventRepository, times(1)).save(eventCaptor.capture());
        CaravanJourneyEvent savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getEventType()).isEqualTo("JOURNEY_START");
    }

    @Test
    void startJourney_fromPreparing_changesStatusToTraveling() {
        VirtualCaravan caravan = new VirtualCaravan();
        caravan.setId(1L);
        caravan.setStatus("PREPARING");
        caravan.setRouteId(1L);
        List<Waypoint> waypoints = createStandardWaypoints();

        when(virtualCaravanRepository.findById(1L)).thenReturn(Optional.of(caravan));
        when(waypointRepository.findByRouteIdOrderByWaypointOrderAsc(1L)).thenReturn(waypoints);
        when(virtualCaravanRepository.save(any(VirtualCaravan.class))).thenReturn(caravan);
        when(caravanJourneyEventRepository.save(any(CaravanJourneyEvent.class))).thenAnswer(inv -> {
            CaravanJourneyEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        VirtualCaravanDTO result = vrCaravanService.startJourney(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("TRAVELING");
        assertThat(caravan.getStartedAt()).isNotNull();
    }

    @Test
    void pauseJourney_travelingToResting_statusChanges() {
        VirtualCaravan caravan = new VirtualCaravan();
        caravan.setId(1L);
        caravan.setStatus("TRAVELING");

        when(virtualCaravanRepository.findById(1L)).thenReturn(Optional.of(caravan));
        when(virtualCaravanRepository.save(any(VirtualCaravan.class))).thenReturn(caravan);

        VirtualCaravanDTO result = vrCaravanService.pauseJourney(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("RESTING");
    }

    @Test
    void resumeJourney_restingBackToTraveling_statusChanges() {
        VirtualCaravan caravan = new VirtualCaravan();
        caravan.setId(1L);
        caravan.setStatus("RESTING");

        when(virtualCaravanRepository.findById(1L)).thenReturn(Optional.of(caravan));
        when(virtualCaravanRepository.save(any(VirtualCaravan.class))).thenReturn(caravan);

        VirtualCaravanDTO result = vrCaravanService.resumeJourney(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("TRAVELING");
    }

    @Test
    void haversineDistance_twoPoints_calculatesCorrectly() {
        double distance = vrCaravanService.haversineDistance(108.94, 34.26, 103.83, 36.06);

        assertThat(distance).isGreaterThan(305.0);
        assertThat(distance).isLessThan(345.0);
    }

    @Test
    void getJourneyEvents_limit10_returns10OrFewer() {
        List<CaravanJourneyEvent> events = new ArrayList<>();
        for (long i = 1; i <= 15; i++) {
            CaravanJourneyEvent e = new CaravanJourneyEvent();
            e.setId(i);
            e.setVirtualCaravanId(1L);
            e.setEventType("TEST_EVENT");
            e.setSeverity("INFO");
            e.setTitle("事件" + i);
            e.setMessage("消息" + i);
            e.setEffectWaterLiters(0.0);
            e.setEffectFoodDays(0.0);
            e.setEffectMorale(0.0);
            e.setEffectGoldCoins(0);
            e.setIsResolved(true);
            e.setEventTime(LocalDateTime.now());
            events.add(e);
        }

        when(caravanJourneyEventRepository.findTop20ByVirtualCaravanIdOrderByEventTimeDesc(1L)).thenReturn(events);

        List<CaravanJourneyEventDTO> result = vrCaravanService.getJourneyEvents(1L, 10);

        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(10);
    }

    @Test
    void triggerRandomEvent_positiveEvent_shouldIncreaseGoldAndMorale() {
        VirtualCaravan caravan = new VirtualCaravan();
        caravan.setId(1L);
        caravan.setWaterSupplyLiters(3000.0);
        caravan.setFoodSupplyDays(30.0);
        caravan.setMorale(80.0);
        caravan.setGoldCoins(100);
        caravan.setCurrentPosition(geometryFactory.createPoint(new Coordinate(108.94, 34.26)));

        Waypoint currentWp = createWaypoint(1L, 1L, "西安", 108.94, 34.26, 1, true, 800.0);

        JourneyEventConfig oasisEvent = new JourneyEventConfig();
        oasisEvent.setId(1L);
        oasisEvent.setEventType("OASIS_DISCOVERED");
        oasisEvent.setEventName("发现绿洲");
        oasisEvent.setDescription("发现了一片隐藏的绿洲！");
        oasisEvent.setSeverity("SUCCESS");
        oasisEvent.setTerrainTypes("ALL");
        oasisEvent.setMinOccurrenceProb(0.3);
        oasisEvent.setMaxOccurrenceProb(0.3);
        oasisEvent.setWaterEffectMin(500.0);
        oasisEvent.setWaterEffectMax(500.0);
        oasisEvent.setFoodEffectMin(5.0);
        oasisEvent.setFoodEffectMax(5.0);
        oasisEvent.setMoraleEffectMin(15.0);
        oasisEvent.setMoraleEffectMax(15.0);
        oasisEvent.setGoldEffectMin(50);
        oasisEvent.setGoldEffectMax(50);
        oasisEvent.setIsPositive(true);

        when(journeyEventConfigRepository.findAll()).thenReturn(Collections.singletonList(oasisEvent));
        when(caravanJourneyEventRepository.save(any(CaravanJourneyEvent.class))).thenAnswer(inv -> {
            CaravanJourneyEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        Integer goldBefore = caravan.getGoldCoins();
        Double moraleBefore = caravan.getMorale();

        CaravanJourneyEventDTO result = vrCaravanService.triggerRandomEvent(caravan, currentWp);

        assertThat(result).isNotNull();
        assertThat(caravan.getGoldCoins()).isGreaterThan(goldBefore);
        assertThat(caravan.getMorale()).isGreaterThan(moraleBefore);
    }

    @Test
    void startJourney_fromTravelingState_throwsIllegalState() {
        VirtualCaravan caravan = new VirtualCaravan();
        caravan.setId(1L);
        caravan.setStatus("TRAVELING");

        when(virtualCaravanRepository.findById(1L)).thenReturn(Optional.of(caravan));

        assertThrows(IllegalStateException.class, () -> vrCaravanService.startJourney(1L));
    }

    @Test
    void pauseJourney_fromRestingState_throwsIllegalState() {
        VirtualCaravan caravan = new VirtualCaravan();
        caravan.setId(1L);
        caravan.setStatus("RESTING");

        when(virtualCaravanRepository.findById(1L)).thenReturn(Optional.of(caravan));

        assertThrows(IllegalStateException.class, () -> vrCaravanService.pauseJourney(1L));
    }

    @Test
    void createVirtualCaravan_insufficientWaypoints_throwsIllegalArgument() {
        CreateVirtualCaravanRequest req = createStandardRequest();
        Waypoint onlyWp = createWaypoint(1L, 1L, "西安", 108.94, 34.26, 1, true, 800.0);

        when(waypointRepository.findByRouteIdOrderByWaypointOrderAsc(1L)).thenReturn(Collections.singletonList(onlyWp));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vrCaravanService.createVirtualCaravan(req));
        assertThat(ex.getMessage()).contains("航点数量不足");
    }

    @Test
    void getVirtualCaravan_wrongId_throwsIllegalArgument() {
        when(virtualCaravanRepository.findById(99999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vrCaravanService.getVirtualCaravan(99999L));
        assertThat(ex.getMessage()).contains("驼队不存在");
    }

    @Test
    void deleteVirtualCaravan_wrongId_throwsIllegalArgument() {
        when(virtualCaravanRepository.existsById(99999L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vrCaravanService.deleteVirtualCaravan(99999L));
        assertThat(ex.getMessage()).contains("驼队不存在");
    }

    @Test
    void startJourney_nullCaravanId_wrappedCorrectly() {
        when(virtualCaravanRepository.findById(99999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> vrCaravanService.startJourney(99999L));
    }

    @Test
    void getCaravanBySession_noCaravans_returnsNull() {
        when(virtualCaravanRepository.findBySessionId("nonexistent-session")).thenReturn(Collections.emptyList());

        VirtualCaravanDTO result = vrCaravanService.getCaravanBySession("nonexistent-session");

        assertThat(result).isNull();
    }

    @Test
    void createVirtualCaravan_shouldSetDefaultSpeedMultiplier() {
        CreateVirtualCaravanRequest req = createStandardRequest();
        List<Waypoint> waypoints = createStandardWaypoints();
        CargoWaterConfig waterConfig = createStandardWaterConfig();

        when(waypointRepository.findByRouteIdOrderByWaypointOrderAsc(1L)).thenReturn(waypoints);
        when(cargoWaterConfigRepository.findByCargoType("SILK")).thenReturn(Optional.of(waterConfig));
        when(virtualCaravanRepository.save(any(VirtualCaravan.class))).thenAnswer(inv -> {
            VirtualCaravan c = inv.getArgument(0);
            c.setId(100L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });
        when(caravanJourneyEventRepository.save(any(CaravanJourneyEvent.class))).thenAnswer(inv -> {
            CaravanJourneyEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        ArgumentCaptor<VirtualCaravan> caravanCaptor = ArgumentCaptor.forClass(VirtualCaravan.class);

        vrCaravanService.createVirtualCaravan(req);

        verify(virtualCaravanRepository).save(caravanCaptor.capture());
        VirtualCaravan savedCaravan = caravanCaptor.getValue();

        assertThat(savedCaravan.getSpeedMultiplier()).isNotNull();
        assertThat(savedCaravan.getSpeedMultiplier()).isEqualTo(2.0);
    }

    @Test
    void simulateTick_withSpeedMultiplier_shouldMoveFaster() {
        VirtualCaravan caravan = new VirtualCaravan();
        caravan.setId(1L);
        caravan.setStatus("TRAVELING");
        caravan.setRouteId(1L);
        caravan.setSpeedMultiplier(2.0);
        caravan.setCurrentWaypointId(1L);
        caravan.setDistanceTraveledKm(0.0);
        caravan.setCurrentPosition(geometryFactory.createPoint(new Coordinate(108.94, 34.26)));
        caravan.setWaterSupplyLiters(4000.0);
        caravan.setFoodSupplyDays(30.0);
        caravan.setMorale(80.0);
        caravan.setGoldCoins(100);

        List<Waypoint> waypoints = new ArrayList<>();
        Waypoint wp1 = createWaypoint(1L, 1L, "西安", 108.94, 34.26, 1, true, 800.0);
        Waypoint wp2 = createWaypoint(2L, 1L, "兰州", 103.83, 36.06, 2, false, 1500.0);
        Waypoint wp3 = createWaypoint(3L, 1L, "武威", 102.64, 37.93, 3, false, 1500.0);
        waypoints.add(wp1);
        waypoints.add(wp2);
        waypoints.add(wp3);

        when(virtualCaravanRepository.findByStatus("TRAVELING")).thenReturn(Collections.singletonList(caravan));
        when(waypointRepository.findByRouteIdOrderByWaypointOrderAsc(1L)).thenReturn(waypoints);
        when(caravanJourneyEventRepository.save(any(CaravanJourneyEvent.class))).thenAnswer(inv -> {
            CaravanJourneyEvent e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        vrCaravanService.simulateTick();

        ArgumentCaptor<VirtualCaravan> caravanCaptor = ArgumentCaptor.forClass(VirtualCaravan.class);
        verify(virtualCaravanRepository, atLeastOnce()).save(caravanCaptor.capture());

        VirtualCaravan updatedCaravan = caravanCaptor.getAllValues().get(caravanCaptor.getAllValues().size() - 1);
        assertThat(updatedCaravan.getDistanceTraveledKm()).isGreaterThan(0);

        double expectedMoveDistance = VrCaravanService.SIMULATED_DAYS_PER_TICK * VrCaravanService.DEFAULT_ANCIENT_DAILY_DISTANCE_KM;
        if (updatedCaravan.getSpeedMultiplier() != null) {
            expectedMoveDistance *= updatedCaravan.getSpeedMultiplier();
        }

        double distanceToWp2 = vrCaravanService.haversineDistance(108.94, 34.26, 103.83, 36.06);
        if (expectedMoveDistance < distanceToWp2) {
            assertThat(updatedCaravan.getDistanceTraveledKm()).isEqualTo(expectedMoveDistance);
        }
    }
}
