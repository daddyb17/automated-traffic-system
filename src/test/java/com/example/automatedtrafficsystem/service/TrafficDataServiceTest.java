package com.example.automatedtrafficsystem.service;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrafficDataServiceTest {

    @Mock
    private TrafficDataRepository trafficDataRepository;

    @InjectMocks
    private TrafficDataService trafficDataService;

    private TrafficData data1, data2, data3, data4;

    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDateTime now = LocalDateTime.now();
        data1 = new TrafficData(1L, now.minusHours(1), 10, now);
        data2 = new TrafficData(2L, now.minusHours(2), 20, now.minusHours(1));
        data3 = new TrafficData(3L, now.minusHours(3), 15, now.minusHours(2));
        data4 = new TrafficData(4L, now.minusDays(1), 5, now.minusDays(1));
    }

    @Test
    void saveTrafficData_ShouldSaveAndReturnTrafficData() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        TrafficData trafficData = new TrafficData(timestamp, 10);
        when(trafficDataRepository.save(any(TrafficData.class))).thenReturn(trafficData);

        // Act
        TrafficData savedData = trafficDataService.saveTrafficData(timestamp, 10);

        // Assert
        assertNotNull(savedData);
        assertEquals(10, savedData.getCarCount());
        assertEquals(timestamp, savedData.getTimestamp());
        verify(trafficDataRepository, times(1)).save(any(TrafficData.class));
    }

    @Test
    void getTotalCars_ShouldReturnSumOfAllCarCounts() {
        // Arrange
        when(trafficDataRepository.findAll()).thenReturn(Arrays.asList(data1, data2, data3));
        
        // Act
        int totalCars = trafficDataService.getTotalCars();
        
        // Assert
        assertEquals(45, totalCars); // 10 + 20 + 15
        verify(trafficDataRepository, times(1)).findAll();
    }

    @Test
    void getDailyCarCounts_ShouldGroupByDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDateTime todayMorning = today.atTime(9, 0);
        LocalDateTime todayAfternoon = today.atTime(14, 30);
        LocalDate yesterday = today.minusDays(1);
        
        TrafficData todayData1 = new TrafficData(1L, todayMorning, 10, todayMorning);
        TrafficData todayData2 = new TrafficData(2L, todayAfternoon, 20, todayAfternoon);
        TrafficData yesterdayData = new TrafficData(3L, yesterday.atTime(10, 0), 15, yesterday.atTime(10, 0));
        
        when(trafficDataRepository.findAll()).thenReturn(Arrays.asList(todayData1, todayData2, yesterdayData));
        
        // Act
        Map<LocalDate, Integer> dailyCounts = trafficDataService.getDailyCarCounts();
        
        // Assert
        assertEquals(2, dailyCounts.size());
        assertEquals(30, dailyCounts.get(today)); // 10 + 20 for today
        assertEquals(15, dailyCounts.get(yesterday)); // 15 for yesterday
        verify(trafficDataRepository, times(1)).findAll();
    }

    @Test
    void getTopThreeHalfHours_ShouldReturnTopThreeRecords() {
        // Arrange
        when(trafficDataRepository.findTopByCarCount(3)).thenReturn(Arrays.asList(data2, data3, data1));
        
        // Act
        List<TrafficData> topThree = trafficDataService.getTopThreeHalfHours();
        
        // Assert
        assertEquals(3, topThree.size());
        assertEquals(20, topThree.get(0).getCarCount()); // Highest count first
        verify(trafficDataRepository, times(1)).findTopByCarCount(3);
    }

    @Test
    void findLeastCarsContiguousPeriod_WithLessThanThreeRecords_ShouldReturnAll() {
        // Arrange
        when(trafficDataRepository.findAllByOrderByTimestampAsc()).thenReturn(Arrays.asList(data1, data2));
        
        // Act
        List<TrafficData> result = trafficDataService.findLeastCarsContiguousPeriod();
        
        // Assert
        assertEquals(2, result.size());
        verify(trafficDataRepository, times(1)).findAllByOrderByTimestampAsc();
    }

    @Test
    void processTrafficDataFile_WithValidContent_ShouldProcessAllLines() {
        // Arrange
        String fileContent = "2023-01-01T12:00:00 10\n2023-01-01T12:30:00 20";
        when(trafficDataRepository.save(any(TrafficData.class))).thenReturn(new TrafficData());
        
        // Act & Assert
        assertDoesNotThrow(() -> trafficDataService.processTrafficDataFile(fileContent));
        verify(trafficDataRepository, times(2)).save(any(TrafficData.class));
    }
}
