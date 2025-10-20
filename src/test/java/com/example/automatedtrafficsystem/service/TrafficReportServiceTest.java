package com.example.automatedtrafficsystem.service;

import com.example.automatedtrafficsystem.model.TrafficData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrafficReportServiceTest {

    @Mock
    private TrafficDataService trafficDataService;

    @InjectMocks
    private TrafficReportService trafficReportService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate today = LocalDate.now();

    private TrafficData createTrafficData(LocalDateTime timestamp, int carCount) {
        TrafficData data = new TrafficData();
        data.setTimestamp(timestamp);
        data.setCarCount(carCount);
        return data;
    }

    @BeforeEach
    void setUp() {
        trafficReportService = new TrafficReportService(trafficDataService);
    }

    @Test
    void generateReport_ReturnsFormattedReport() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(today, LocalTime.of(12, 0));
        TrafficData data1 = createTrafficData(baseTime, 10);
        TrafficData data2 = createTrafficData(baseTime.plusMinutes(30), 15);
        TrafficData data3 = createTrafficData(baseTime.plusHours(1), 20);
        
        // Mock the service methods with realistic data
        int totalCars = data1.getCarCount() + data2.getCarCount() + data3.getCarCount();
        when(trafficDataService.getTotalCars()).thenReturn(totalCars);
        when(trafficDataService.getDailyCarCounts())
            .thenReturn(Collections.singletonMap(today, totalCars));
        when(trafficDataService.getTopThreeHalfHours())
            .thenReturn(Arrays.asList(data3, data2, data1)); // Sorted by car count descending
        when(trafficDataService.findLeastCarsContiguousPeriod())
            .thenReturn(Arrays.asList(data1, data2, data3)); // Sorted by timestamp ascending

        // Act
        String report = trafficReportService.generateReport();

        // Assert - Check for required sections in the report
        assertNotNull(report, "Report should not be null");
        
        // Verify the report contains the expected sections
        String[] expectedSections = {
            "Total cars seen: " + totalCars,
            "Daily car counts:",
            "Top 3 half hours with most cars:",
            "1.5 hour period with least cars (3 contiguous half-hour records):"
        };
        
        for (String section : expectedSections) {
            assertTrue(report.contains(section), 
                String.format("Report should contain: %s", section));
        }
        
        // Verify data is included in the report with proper formatting
        String reportLower = report.toLowerCase();
        assertTrue(reportLower.contains(String.valueOf(data1.getCarCount()).toLowerCase()), 
            "Report should contain car count from data1");
        assertTrue(reportLower.contains(String.valueOf(data2.getCarCount()).toLowerCase()), 
            "Report should contain car count from data2");
        assertTrue(reportLower.contains(String.valueOf(data3.getCarCount()).toLowerCase()), 
            "Report should contain car count from data3");
            
        // Verify service interactions
        verify(trafficDataService, times(1)).getTotalCars();
        verify(trafficDataService, times(1)).getDailyCarCounts();
        verify(trafficDataService, times(1)).getTopThreeHalfHours();
        verify(trafficDataService, times(1)).findLeastCarsContiguousPeriod();
    }

    @Test
    void getReportAsJson_ReturnsValidJsonStructure() {
        // Arrange
        TrafficData data1 = createTrafficData(now, 10);
        TrafficData data2 = createTrafficData(now.plusMinutes(30), 15);
        
        int totalCars = 25;
        Map<LocalDate, Integer> dailyCounts = Collections.singletonMap(today, totalCars);
        List<TrafficData> topThree = Arrays.asList(data2, data1);
        List<TrafficData> leastCarsPeriod = Arrays.asList(data1, data2);
        
        // Mock the service methods with realistic data
        when(trafficDataService.getTotalCars()).thenReturn(totalCars);
        when(trafficDataService.getDailyCarCounts()).thenReturn(dailyCounts);
        when(trafficDataService.getTopThreeHalfHours()).thenReturn(topThree);
        when(trafficDataService.findLeastCarsContiguousPeriod()).thenReturn(leastCarsPeriod);

        // Act
        Map<String, Object> report = trafficReportService.getReportAsJson();

        // Assert - Basic structure
        assertNotNull(report, "Report should not be null");
        
        // Verify required fields and their types
        assertTrue(report.containsKey("totalCars"), "Report should contain 'totalCars'");
        assertEquals(totalCars, report.get("totalCars"), "Total cars count should match");
        
        assertTrue(report.containsKey("dailyCounts"), "Report should contain 'dailyCounts'");
        assertTrue(report.get("dailyCounts") instanceof Map, "dailyCounts should be a Map");
        
        assertTrue(report.containsKey("topThreeHalfHours"), "Report should contain 'topThreeHalfHours'");
        assertTrue(report.get("topThreeHalfHours") instanceof List, "topThreeHalfHours should be a List");
        
        assertTrue(report.containsKey("leastCarsPeriod"), "Report should contain 'leastCarsPeriod'");
        assertTrue(report.get("leastCarsPeriod") instanceof List, "leastCarsPeriod should be a List");
        
        // Verify the structure of topThreeHalfHours
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topThreeData = (List<Map<String, Object>>) report.get("topThreeHalfHours");
        assertEquals(2, topThreeData.size(), "Should contain 2 top records");
        assertTrue(topThreeData.get(0).containsKey("timestamp"), "Top record should contain timestamp");
        assertTrue(topThreeData.get(0).containsKey("carCount"), "Top record should contain carCount");
        
        // Verify the structure of leastCarsPeriod
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> leastCarsData = (List<Map<String, Object>>) report.get("leastCarsPeriod");
        assertEquals(2, leastCarsData.size(), "Should contain 2 least cars records");
        assertTrue(leastCarsData.get(0).containsKey("timestamp"), "Least cars record should contain timestamp");
        assertTrue(leastCarsData.get(0).containsKey("carCount"), "Least cars record should contain carCount");
        
        // Verify service interactions
        verify(trafficDataService).getTotalCars();
        verify(trafficDataService).getDailyCarCounts();
        verify(trafficDataService).getTopThreeHalfHours();
        verify(trafficDataService).findLeastCarsContiguousPeriod();
    }
}
