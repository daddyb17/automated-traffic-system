package com.example.automatedtrafficsystem.ai;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.ChatClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrafficAnalysisServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private TrafficDataRepository trafficDataRepository;

    @InjectMocks
    private TrafficAnalysisService trafficAnalysisService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        trafficAnalysisService = new TrafficAnalysisService(
            chatClient, 
            trafficDataRepository
        );
    }

    @Test
    void predictTraffic_WithValidData_ReturnsPrediction() {
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);

        TrafficData historicalData1 = new TrafficData(now.minusDays(1), 10);
        TrafficData historicalData2 = new TrafficData(now.minusDays(2), 15);
        TrafficData historicalData3 = new TrafficData(now.minusDays(3), 20);
        
        when(trafficDataRepository.findByTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            startTime.minusDays(30),
            startTime
        )).thenReturn(Arrays.asList(historicalData1, historicalData2, historicalData3));

        TrafficPrediction result = trafficAnalysisService.predictTraffic(startTime, endTime);

        assertNotNull(result);
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());

        assertTrue(List.of("LOW", "MODERATE", "HIGH").contains(result.getTrafficCondition()));

        assertTrue(result.getConfidenceScore() > 0 && result.getConfidenceScore() <= 1.0);

        assertTrue(result.getAverageSpeed() > 0, "Average speed should be positive");
        assertTrue(result.getExpectedVolume() >= 0, "Expected volume should be non-negative");
        assertTrue(result.getExpectedTravelTimeMinutes() > 0, "Expected travel time should be positive");
        assertNotNull(result.getDetails());
    }

    @Test
    void predictTraffic_WithNoHistoricalData_ThrowsException() {
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);

        when(trafficDataRepository.findByTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(any(), any()))
            .thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> {
            trafficAnalysisService.predictTraffic(startTime, endTime);
        });
    }
}
