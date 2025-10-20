package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.ai.TrafficAnalysisService;
import com.example.automatedtrafficsystem.ai.TrafficPrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TrafficAIControllerTest {

    @Mock
    private TrafficAnalysisService trafficAnalysisService;

    @InjectMocks
    private TrafficAIController trafficAIController;

    private MockMvc mockMvc;
    private final LocalDate today = LocalDate.now();
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trafficAIController).build();
    }

    @Test
    void analyzeTrafficPatterns_WithValidDates_ShouldReturnAnalysis() throws Exception {
        // Arrange
        String analysisResult = "Traffic analysis result";
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;
        
        when(trafficAnalysisService.analyzeTrafficPatterns(startDate, endDate))
            .thenReturn(analysisResult);

        // Act & Assert
        mockMvc.perform(get("/api/ai/traffic/analyze")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(analysisResult)));
    }

    @Test
    void predictTraffic_WithValidTimeRange_ShouldReturnPrediction() throws Exception {
        // Arrange
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);
        TrafficPrediction prediction = TrafficPrediction.builder()
                .startTime(startTime)
                .endTime(endTime)
                .trafficCondition("HEAVY")
                .confidenceScore(0.85)
                .details("High traffic expected due to rush hour")
                .averageSpeed(25.5)
                .expectedVolume(150)
                .potentialIncidents("Possible congestion on Main Street")
                .alternativeRoutes("Use Highway 101 as alternative")
                .expectedTravelTimeMinutes(45)
                .build();
        
        when(trafficAnalysisService.predictTraffic(startTime, endTime))
            .thenReturn(prediction);

        // Act & Assert
        mockMvc.perform(get("/api/ai/traffic/predict")
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists())
                .andExpect(jsonPath("$.trafficCondition").value("HEAVY"))
                .andExpect(jsonPath("$.confidenceScore").value(0.85))
                .andExpect(jsonPath("$.details").value("High traffic expected due to rush hour"))
                .andExpect(jsonPath("$.averageSpeed").value(25.5))
                .andExpect(jsonPath("$.expectedVolume").value(150));
    }
}
