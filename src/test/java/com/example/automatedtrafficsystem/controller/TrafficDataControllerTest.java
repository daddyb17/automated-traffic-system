package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.exception.GlobalExceptionHandler;
import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.service.TrafficDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrafficDataControllerTest {

    @Mock
    private TrafficDataService trafficDataService;

    @InjectMocks
    private TrafficDataController trafficDataController;

    private MockMvc mockMvc;

    private TrafficData data1;
    private TrafficData data2;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trafficDataController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

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
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "traffic.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "2023-01-01T12:00:00 10\n2023-01-01T12:30:00 20".getBytes()
        );

        doNothing().when(trafficDataService).processTrafficDataFile(anyString());

        mockMvc.perform(multipart("/api/traffic/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().string("File processed successfully"));
    }

    @Test
    void uploadTrafficData_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "".getBytes()
        );

        mockMvc.perform(multipart("/api/traffic/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("File cannot be empty")))
                .andExpect(jsonPath("$.errorCode", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.path", is("/api/traffic/upload")))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void getTotalCars_ShouldReturnTotalCount() throws Exception {
        when(trafficDataService.getTotalCars()).thenReturn(30L);

        mockMvc.perform(get("/api/v1/traffic/total"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(30)));
    }

    @Test
    void getDailyCarCounts_ShouldReturnDailyCounts() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Map<LocalDate, Long> dailyCounts = new HashMap<>();
        dailyCounts.put(today, 30L);
        dailyCounts.put(yesterday, 15L);
        when(trafficDataService.getDailyCarCounts()).thenReturn(dailyCounts);

        mockMvc.perform(get("/api/v1/traffic/daily"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$." + today, is(30)))
                .andExpect(jsonPath("$." + yesterday, is(15)));
    }

    @Test
    void getTopThreeHalfHours_ShouldReturnTopThreeRecords() throws Exception {
        List<TrafficData> topThree = Arrays.asList(data2, data1);
        when(trafficDataService.getTopThreeHalfHours()).thenReturn(topThree);

        mockMvc.perform(get("/api/v1/traffic/top-three"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].carCount", is(20)))
                .andExpect(jsonPath("$[1].carCount", is(10)));
    }

    @Test
    void getLeastCarsPeriod_ShouldReturnContiguousPeriod() throws Exception {
        when(trafficDataService.findLeastCarsContiguousPeriod()).thenReturn(Arrays.asList(data1, data2));

        mockMvc.perform(get("/api/v1/traffic/least-cars-period"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void addTrafficData_WithValidData_ShouldReturnSavedData() throws Exception {
        LocalDateTime timestamp = LocalDateTime.now();
        TrafficData savedData = new TrafficData();
        savedData.setId(1L);
        savedData.setTimestamp(timestamp);
        savedData.setCarCount(15);
        savedData.setCreatedAt(timestamp);

        when(trafficDataService.saveTrafficData(any(LocalDateTime.class), anyInt())).thenReturn(savedData);

        mockMvc.perform(post("/api/v1/traffic")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("timestamp", timestamp.toString())
                        .param("carCount", "15"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.carCount", is(15)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void addTrafficData_WithInvalidTimestamp_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/traffic")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("timestamp", "invalid-date")
                        .param("carCount", "15"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void getTrafficData_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        List<TrafficData> trafficDataList = Arrays.asList(data1, data2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<TrafficData> page = new PageImpl<>(trafficDataList, pageable, trafficDataList.size());
        when(trafficDataService.getAllTrafficData(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/traffic")
                        .param("version", "1")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "timestamp,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getTrafficData_WithInvalidSort_ShouldFallbackToTimestamp() throws Exception {
        List<TrafficData> trafficDataList = Arrays.asList(data1, data2);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<TrafficData> page = new PageImpl<>(trafficDataList, pageable, trafficDataList.size());
        when(trafficDataService.getAllTrafficData(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/traffic")
                        .param("version", "1")
                        .param("sort", "invalidField"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(trafficDataService).getAllTrafficData(pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("timestamp");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getTrafficStatsV2_ShouldReturnStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCars", 100L);
        stats.put("averageCarsPerDay", 10.5);
        stats.put("totalRecords", 8);
        when(trafficDataService.getTrafficStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/v2/traffic/stats")
                        .param("version", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCars").value(100))
                .andExpect(jsonPath("$.averageCarsPerDay").value(10.5))
                .andExpect(jsonPath("$.totalRecords").value(8));
    }

    @Test
    void uploadTrafficData_WithServiceFailure_ShouldReturnInternalServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "error.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "2023-01-01T12:00:00 10".getBytes()
        );

        doThrow(new RuntimeException("File processing failed")).when(trafficDataService).processTrafficDataFile(anyString());

        mockMvc.perform(multipart("/api/v1/traffic/upload")
                        .file(file)
                        .param("version", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("INTERNAL_SERVER_ERROR")));
    }
}
