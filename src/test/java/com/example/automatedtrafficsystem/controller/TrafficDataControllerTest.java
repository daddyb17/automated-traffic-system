package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.exception.GlobalExceptionHandler;
import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.service.TrafficDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
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
        mockMvc = MockMvcBuilders.standaloneSetup(trafficDataController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Setup test data
        data1 = new TrafficData();
        data1.setId(1L);
        data1.setTimestamp(now.minusHours(1));
        data1.setCarCount(10);
        data1.setCreatedAt(now);
        
        data2 = new TrafficData();
        data2.setId(2L);
        data2.setTimestamp(now.minusHours(2));
        data2.setCarCount(20);
        data2.setCreatedAt(now.minusHours(1));
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
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
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
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("File cannot be empty")))
                .andExpect(jsonPath("$.errorCode", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void getTotalCars_ShouldReturnTotalCount() throws Exception {
        // Arrange
        when(trafficDataService.getTotalCars()).thenReturn(30);

        // Act & Assert
        mockMvc.perform(get("/api/v1/traffic/total"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(30)));
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
        mockMvc.perform(get("/api/v1/traffic/daily"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$." + today.toString(), is(30)))
                .andExpect(jsonPath("$." + yesterday.toString(), is(15)));
    }

    @Test
    void getTopThreeHalfHours_ShouldReturnTopThreeRecords() throws Exception {
        // Arrange
        List<TrafficData> topThree = Arrays.asList(data2, data1);
        when(trafficDataService.getTopThreeHalfHours()).thenReturn(topThree);

        // Act & Assert
        mockMvc.perform(get("/api/v1/traffic/top-three"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
        mockMvc.perform(get("/api/v1/traffic/least-cars-period"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void addTrafficData_WithValidData_ShouldReturnSavedData() throws Exception {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        TrafficData savedData = new TrafficData();
        savedData.setId(1L);
        savedData.setTimestamp(timestamp);
        savedData.setCarCount(15);
        savedData.setCreatedAt(timestamp);
        
        when(trafficDataService.saveTrafficData(any(LocalDateTime.class), anyInt()))
            .thenReturn(savedData);

        // Act & Assert
        mockMvc.perform(post("/api/v1/traffic")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("timestamp", timestamp.toString())
                .param("carCount", "15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.carCount", is(15)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void addTrafficData_WithInvalidTimestamp_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/traffic")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("timestamp", "invalid-date")
                .param("carCount", "15"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void getTrafficData_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        // Arrange
        List<TrafficData> trafficDataList = Arrays.asList(data1, data2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<TrafficData> page = new PageImpl<>(trafficDataList, pageable, trafficDataList.size());
        when(trafficDataService.getAllTrafficData(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/traffic")
                .param("version", "1")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "timestamp,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].carCount", is(10)))
                .andExpect(jsonPath("$.content[1].carCount", is(20)));
    }

    @Test
    void getTrafficStatsV2_ShouldReturnStatistics() throws Exception {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCars", 100);
        stats.put("averageCars", 10.5);
        stats.put("maxCars", 30);
        stats.put("minCars", 5);
        
        when(trafficDataService.getTrafficStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v2/traffic/stats")
                .param("version", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCars").value(100))
                .andExpect(jsonPath("$.averageCars").value(10.5))
                .andExpect(jsonPath("$.maxCars").value(30))
                .andExpect(jsonPath("$.minCars").value(5));
    }

    @Test
    void uploadTrafficData_WithIOException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        String fileContent = "2023-01-01T12:00:00 10";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "error.txt", 
            MediaType.TEXT_PLAIN_VALUE, 
            fileContent.getBytes()
        );

        doThrow(new RuntimeException("File processing failed"))
            .when(trafficDataService).processTrafficDataFile(anyString());

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/traffic/upload")
                .file(file)
                .param("version", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("INTERNAL_SERVER_ERROR")));
    }

    @Test
    void getTrafficData_WithInvalidSort_ShouldUseDefaultSort() throws Exception {
        // Arrange
        List<TrafficData> trafficDataList = Arrays.asList(data1, data2);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<TrafficData> page = new PageImpl<>(trafficDataList, pageable, trafficDataList.size());
        when(trafficDataService.getAllTrafficData(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/traffic")
                .param("version", "1")
                .param("sort", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }
}
