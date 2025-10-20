package com.example.automatedtrafficsystem.ai;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TrafficAnalysisService trafficAnalysisService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        trafficAnalysisService = new TrafficAnalysisService(
            chatClient, 
            trafficDataRepository,
            objectMapper
        );
    }

    @Test
    void predictTraffic_WithValidData_ReturnsPrediction() {
        // Arrange
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);
        
        // Create historical data with varying car counts to test the prediction logic
        TrafficData historicalData1 = new TrafficData(now.minusDays(1), 10);
        TrafficData historicalData2 = new TrafficData(now.minusDays(2), 15);
        TrafficData historicalData3 = new TrafficData(now.minusDays(3), 20);
        
        when(trafficDataRepository.findByTimestampBetween(
            startTime.minusDays(30),
            startTime
        )).thenReturn(Arrays.asList(historicalData1, historicalData2, historicalData3));

        // Act
        TrafficPrediction result = trafficAnalysisService.predictTraffic(startTime, endTime);

        // Assert
        assertNotNull(result);
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        
        // Verify traffic condition is one of the expected values
        assertTrue(List.of("LOW", "MODERATE", "HIGH").contains(result.getTrafficCondition()));
        
        // Verify confidence score is within valid range
        assertTrue(result.getConfidenceScore() > 0 && result.getConfidenceScore() <= 1.0);
        
        // Verify calculated fields
        assertTrue(result.getAverageSpeed() > 0, "Average speed should be positive");
        assertTrue(result.getExpectedVolume() >= 0, "Expected volume should be non-negative");
        assertTrue(result.getExpectedTravelTimeMinutes() > 0, "Expected travel time should be positive");
        assertNotNull(result.getDetails());
    }

    @Test
    void predictTraffic_WithNoHistoricalData_ThrowsException() {
        // Arrange
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);
        
        when(trafficDataRepository.findByTimestampBetween(any(), any()))
            .thenReturn(List.of());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            trafficAnalysisService.predictTraffic(startTime, endTime);
        });
    }
}
