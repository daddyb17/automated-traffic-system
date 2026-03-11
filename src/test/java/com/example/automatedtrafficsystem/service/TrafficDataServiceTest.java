package com.example.automatedtrafficsystem.service;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class TrafficDataServiceTest {

    @Mock
    private TrafficDataRepository trafficDataRepository;

    @InjectMocks
    private TrafficDataService trafficDataService;

    private TrafficData data1;
    private TrafficData data2;
    private TrafficData data3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        data1 = new TrafficData(1L, now.minusHours(1), 10, now);
        data2 = new TrafficData(2L, now.minusHours(2), 20, now.minusHours(1));
        data3 = new TrafficData(3L, now.minusHours(3), 15, now.minusHours(2));
    }

    @Test
    void saveTrafficData_ShouldSaveAndReturnTrafficData() {
        LocalDateTime timestamp = LocalDateTime.now();
        TrafficData trafficData = new TrafficData(timestamp, 10);

        when(trafficDataRepository.existsByTimestamp(timestamp)).thenReturn(false);
        when(trafficDataRepository.save(any(TrafficData.class))).thenReturn(trafficData);

        TrafficData savedData = trafficDataService.saveTrafficData(timestamp, 10);

        assertNotNull(savedData);
        assertEquals(10, savedData.getCarCount());
        assertEquals(timestamp, savedData.getTimestamp());
        verify(trafficDataRepository).existsByTimestamp(timestamp);
        verify(trafficDataRepository).save(any(TrafficData.class));
    }

    @Test
    void saveTrafficData_WithExistingTimestamp_ShouldThrow() {
        LocalDateTime timestamp = LocalDateTime.now();
        when(trafficDataRepository.existsByTimestamp(timestamp)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficDataService.saveTrafficData(timestamp, 10)
        );

        assertTrue(ex.getMessage().contains("already exists"));
        verify(trafficDataRepository, never()).save(any(TrafficData.class));
    }

    @Test
    void getTotalCars_ShouldReturnAggregatedCount() {
        when(trafficDataRepository.getTotalCars()).thenReturn(45L);

        long totalCars = trafficDataService.getTotalCars();

        assertEquals(45L, totalCars);
        verify(trafficDataRepository).getTotalCars();
    }

    @Test
    void getDailyCarCounts_ShouldMapRepositoryProjection() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        TrafficDataRepository.DailyTrafficTotalView row1 = new DailyTotalView(today.toString(), 30L);
        TrafficDataRepository.DailyTrafficTotalView row2 = new DailyTotalView(yesterday.toString(), 15L);
        when(trafficDataRepository.findDailyTrafficTotals()).thenReturn(Arrays.asList(row1, row2));

        Map<LocalDate, Long> dailyCounts = trafficDataService.getDailyCarCounts();

        assertEquals(2, dailyCounts.size());
        assertEquals(30L, dailyCounts.get(today));
        assertEquals(15L, dailyCounts.get(yesterday));
    }

    @Test
    void getTopThreeHalfHours_ShouldReturnTopThreeRecords() {
        when(trafficDataRepository.findTop3ByOrderByCarCountDesc())
                .thenReturn(Arrays.asList(data2, data3, data1));

        List<TrafficData> topThree = trafficDataService.getTopThreeHalfHours();

        assertEquals(3, topThree.size());
        assertEquals(20, topThree.get(0).getCarCount());
        verify(trafficDataRepository).findTop3ByOrderByCarCountDesc();
    }

    @Test
    void findLeastCarsContiguousPeriod_WithLessThanThreeRecords_ShouldReturnAll() {
        when(trafficDataRepository.findAllByOrderByTimestampAsc()).thenReturn(Arrays.asList(data1, data2));

        List<TrafficData> result = trafficDataService.findLeastCarsContiguousPeriod();

        assertEquals(2, result.size());
        verify(trafficDataRepository).findAllByOrderByTimestampAsc();
    }

    @Test
    void processTrafficDataFile_WithValidContent_ShouldPersistBatch() {
        String fileContent = "2023-01-01T12:00:00 10\n2023-01-01T12:30:00 20";

        when(trafficDataRepository.existsByTimestamp(any(LocalDateTime.class))).thenReturn(false);

        assertDoesNotThrow(() -> trafficDataService.processTrafficDataFile(fileContent));

        ArgumentCaptor<List<TrafficData>> captor = ArgumentCaptor.forClass(List.class);
        verify(trafficDataRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void processTrafficDataFile_WithDuplicateTimestampInFile_ShouldThrow() {
        String fileContent = "2023-01-01T12:00:00 10\n2023-01-01T12:00:00 20";
        when(trafficDataRepository.existsByTimestamp(any(LocalDateTime.class))).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> trafficDataService.processTrafficDataFile(fileContent)
        );

        assertTrue(ex.getMessage().contains("Duplicate timestamp"));
        verify(trafficDataRepository, never()).saveAll(any());
    }

    private record DailyTotalView(String trafficDate, long totalCars) implements TrafficDataRepository.DailyTrafficTotalView {
        @Override
        public String getTrafficDate() {
            return trafficDate;
        }

        @Override
        public long getTotalCars() {
            return totalCars;
        }
    }
}
