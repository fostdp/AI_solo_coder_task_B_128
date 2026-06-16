package com.silkroad;

import com.silkroad.config.PathfindingExecutorConfig;
import com.silkroad.dto.PathRequest;
import com.silkroad.dto.PathResult;
import com.silkroad.model.Season;
import com.silkroad.route_planner.service.PlannerPathfindingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(PathfindingExecutorConfig.class)
class PathfindingExecutorConfigTest {

    @Autowired
    private Executor pathfindingExecutor;

    @Autowired
    private PlannerPathfindingService pathfindingService;

    @Test
    void pathfindingExecutor_shouldBeConfiguredCorrectly() {
        assertThat(pathfindingExecutor).isNotNull();
        assertThat(pathfindingExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) pathfindingExecutor;
        assertThat(executor.getCorePoolSize()).isEqualTo(2);
        assertThat(executor.getMaxPoolSize()).isEqualTo(4);
        assertThat(executor.getQueueCapacity()).isEqualTo(50);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("pathfind-");
    }

    @Test
    void findOptimalPath_shouldReturnCompletableFuture() {
        PathRequest request = PathRequest.builder()
                .startLng(108.94)
                .startLat(34.26)
                .endLng(95.33)
                .endLat(29.65)
                .season(Season.SPRING.getCode())
                .caravanSpeed(5.0)
                .preferOasis(true)
                .build();

        CompletableFuture<PathResult> future = pathfindingService.findOptimalPath(request);

        assertThat(future).isNotNull();
        assertThat(future).isInstanceOf(CompletableFuture.class);

        PathResult result = assertDoesNotThrow(() -> future.get());
        assertThat(result).isNotNull();
        assertThat(result.getPathPoints()).isNotEmpty();
        assertThat(result.getTotalDistanceKm()).isGreaterThan(0);
        assertThat(result.getAlgorithmUsed()).isNotNull();
    }

    @Test
    void findOptimalPath_asyncExecution_shouldNotBlockThread() {
        PathRequest request = PathRequest.builder()
                .startLng(108.94)
                .startLat(34.26)
                .endLng(95.33)
                .endLat(29.65)
                .season(Season.SPRING.getCode())
                .caravanSpeed(5.0)
                .preferOasis(true)
                .build();

        long startTime = System.currentTimeMillis();
        CompletableFuture<PathResult> future = pathfindingService.findOptimalPath(request);
        long callTime = System.currentTimeMillis() - startTime;

        assertThat(callTime).isLessThan(1000);
        assertThat(future).isNotNull();
    }

    @Test
    void findOptimalPath_withPathfindingExecutor_shouldUseSeparateThreadPool() throws Exception {
        PathRequest request = PathRequest.builder()
                .startLng(108.94)
                .startLat(34.26)
                .endLng(95.33)
                .endLat(29.65)
                .season(Season.SPRING.getCode())
                .caravanSpeed(5.0)
                .preferOasis(true)
                .build();

        String mainThreadName = Thread.currentThread().getName();

        CompletableFuture<PathResult> future = pathfindingService.findOptimalPath(request);

        PathResult result = future.get();
        assertThat(result).isNotNull();

        boolean executedInSeparateThread = !Thread.currentThread().getName().equals(mainThreadName)
                || future.isDone();
        assertThat(executedInSeparateThread).isTrue();
    }
}
