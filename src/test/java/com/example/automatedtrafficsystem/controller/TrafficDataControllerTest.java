package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.service.TrafficDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TrafficDataControllerTest {

    @Mock
    private TrafficDataService trafficDataService;

    @InjectMocks
    private TrafficDataController trafficDataController;

    private MockMvc mockMvc;

    private TrafficData data1, data2;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trafficDataController).build();
        
        // Setup test data
        data1 = new TrafficData(1L, now.minusHours(1), 10, now);
        data2 = new TrafficData(2L, now.minusHours(2), 20, now.minusHours(1));
    }

    @Test
    void uploadTrafficData_WithValidFile_ShouldReturnSuccess() throws Exception {
        // Arrange
        String fileContent = "2023-01-01T12:00:00 10\n2023-01-01T12:30:00 20";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.txt", 
            MediaType.TEXT_PLAIN_VALUE, 
            fileContent.getBytes()
        );

        doNothing().when(trafficDataService).processTrafficDataFile(anyString());

        // Act & Assert
        mockMvc.perform(multipart("/api/traffic/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("File processed successfully"));
    }

    @Test
    void uploadTrafficData_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "empty.txt", 
            MediaType.TEXT_PLAIN_VALUE, 
            "".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/traffic/upload")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Please upload a file")));
    }

    @Test
    void getTotalCars_ShouldReturnTotalCount() throws Exception {
        // Arrange
        when(trafficDataService.getTotalCars()).thenReturn(30);

        // Act & Assert
        mockMvc.perform(get("/api/traffic/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("30"));
    }

    @Test
    void getDailyCarCounts_ShouldReturnDailyCounts() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();
        dailyCounts.put(today, 30);
        dailyCounts.put(yesterday, 15);
        
        when(trafficDataService.getDailyCarCounts()).thenReturn(dailyCounts);

        // Act & Assert
        mockMvc.perform(get("/api/traffic/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$['" + today + "']", is(30)))
                .andExpect(jsonPath("$['" + yesterday + "']", is(15)));
    }

    @Test
    void getTopThreeHalfHours_ShouldReturnTopThreeRecords() throws Exception {
        // Arrange
        List<TrafficData> topThree = Arrays.asList(data2, data1);
        when(trafficDataService.getTopThreeHalfHours()).thenReturn(topThree);

        // Act & Assert
        mockMvc.perform(get("/api/traffic/top-three"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].carCount", is(20)))
                .andExpect(jsonPath("$[1].carCount", is(10)));
    }

    @Test
    void getLeastCarsPeriod_ShouldReturnContiguousPeriod() throws Exception {
        // Arrange
        List<TrafficData> leastCarsPeriod = Arrays.asList(data1, data2);
        when(trafficDataService.findLeastCarsContiguousPeriod()).thenReturn(leastCarsPeriod);

        // Act & Assert
        mockMvc.perform(get("/api/traffic/least-cars-period"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void addTrafficData_WithValidData_ShouldReturnSavedData() throws Exception {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        TrafficData savedData = new TrafficData(timestamp, 15);
        savedData.setId(1L);
        
        when(trafficDataService.saveTrafficData(any(LocalDateTime.class), anyInt()))
            .thenReturn(savedData);

        // Act & Assert
        mockMvc.perform(post("/api/traffic/add")
                .param("timestamp", timestamp.toString())
                .param("carCount", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.carCount", is(15)));
    }

    @Test
    void addTrafficData_WithInvalidTimestamp_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/traffic/add")
                .param("timestamp", "invalid-date")
                .param("carCount", "15"))
                .andExpect(status().isBadRequest());
    }
}
