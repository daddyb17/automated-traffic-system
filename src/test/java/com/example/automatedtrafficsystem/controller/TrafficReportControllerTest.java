package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.service.TrafficReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TrafficReportControllerTest {

    @Mock
    private TrafficReportService trafficReportService;

    @InjectMocks
    private TrafficReportController trafficReportController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trafficReportController).build();
    }

    @Test
    void getTextReport_ShouldReturnTextReport() throws Exception {
        // Arrange
        String reportText = "Total cars seen: 150\n\n" +
                "Daily car counts:\n" +
                "2023-10-20 100\n" +
                "2023-10-21 50\n\n" +
                "Top 3 half hours with most cars:\n" +
                "2023-10-20T08:30 30\n" +
                "2023-10-20T09:00 25\n" +
                "2023-10-20T17:30 20";
                
        when(trafficReportService.generateReport()).thenReturn(reportText);

        // Act & Assert
        mockMvc.perform(get("/api/reports")
                .header("Accept", "text/plain"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString("Total cars seen:")))
                .andExpect(content().string(containsString("Daily car counts:")))
                .andExpect(content().string(containsString("Top 3 half hours with most cars:")));
    }

    @Test
    void getJsonReport_ShouldReturnJsonReport() throws Exception {
        // Arrange
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalCars", 150);
        
        Map<String, Integer> dailyCounts = new HashMap<>();
        dailyCounts.put("2023-10-20", 100);
        dailyCounts.put("2023-10-21", 50);
        reportData.put("dailyCounts", dailyCounts);
        
        when(trafficReportService.getReportAsJson()).thenReturn(reportData);

        // Act & Assert
        mockMvc.perform(get("/api/reports")
                .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCars").exists())
                .andExpect(jsonPath("$.dailyCounts").isMap())
                .andExpect(jsonPath("$.dailyCounts.2023-10-20").value(100))
                .andExpect(jsonPath("$.dailyCounts.2023-10-21").value(50));
    }

    @Test
    void getReport_WithUnacceptableMediaType_ShouldReturnNotAcceptable() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reports")
                .header("Accept", "application/xml"))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void getJsonReport_WithEmptyData_ShouldReturnEmptyJson() throws Exception {
        // Arrange
        when(trafficReportService.getReportAsJson()).thenReturn(Collections.emptyMap());

        // Act & Assert
        mockMvc.perform(get("/api/reports")
                .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void getTextReport_WithEmptyData_ShouldReturnEmptyString() throws Exception {
        // Arrange
        when(trafficReportService.generateReport()).thenReturn("");

        // Act & Assert
        mockMvc.perform(get("/api/reports")
                .header("Accept", "text/plain"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
