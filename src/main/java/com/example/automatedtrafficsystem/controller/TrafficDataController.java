package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.config.ApiVersion;
import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.service.TrafficDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrafficDataController {

    private final TrafficDataService trafficDataService;

    @ApiVersion(1)
    @PostMapping(value = {"/traffic/upload", "/v{version}/traffic/upload"}, 
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Upload traffic data from a file",
              description = "Uploads a text file containing timestamp and car count pairs")
    public ResponseEntity<?> uploadTrafficData(
            @PathVariable(required = false) String version,
            @Parameter(description = "Traffic data file to upload")
            @RequestParam("file") MultipartFile file) {
        log.info("Received file upload request: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "File cannot be empty");
            errorResponse.put("errorCode", "BAD_REQUEST");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            errorResponse.put("details", "The uploaded file is empty");
            
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        try {
            String content = new String(file.getBytes());
            trafficDataService.processTrafficDataFile(content);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("File processed successfully");
        } catch (IOException e) {
            log.error("Error processing file: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to process file");
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            errorResponse.put("details", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @ApiVersion(1)
    @GetMapping("/v{version}/traffic/total")
    @Operation(summary = "Get total number of cars",
              description = "Returns the sum of all car counts in the system")
    public ResponseEntity<Integer> getTotalCars(@PathVariable String version) {
        log.info("Received request to get total number of cars");
        return ResponseEntity.ok(trafficDataService.getTotalCars());
    }

    @ApiVersion(1)
    @GetMapping("/v{version}/traffic/daily")
    @Operation(summary = "Get daily car counts",
              description = "Returns a map of dates to total car counts for each day")
    public ResponseEntity<Map<String, Integer>> getDailyCarCounts(@PathVariable String version) {
        log.info("Received request to get daily car counts");
        Map<LocalDate, Integer> dailyCounts = trafficDataService.getDailyCarCounts();
        Map<String, Integer> result = new TreeMap<>();
        dailyCounts.forEach((date, count) -> result.put(date.toString(), count));
        return ResponseEntity.ok(result);
    }

    @ApiVersion(1)
    @GetMapping("/v{version}/traffic")
    @Operation(summary = "Get all traffic data with pagination")
    public ResponseEntity<Page<TrafficData>> getTrafficData(
            @PathVariable String version,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {
        
        Sort.Direction direction = sort.endsWith("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = sort.split(",")[0];
        Pageable pageable = PageRequest.of(page, size, direction, sortBy);
        
        Page<TrafficData> trafficData = trafficDataService.getAllTrafficData(pageable);
        return ResponseEntity.ok(trafficData);
    }

    @ApiVersion(1)
    @GetMapping("/v{version}/traffic/top-three")
    @Operation(summary = "Get top 3 half-hour periods with most cars",
              description = "Returns the 3 half-hour periods with the highest car counts")
    public ResponseEntity<List<TrafficData>> getTopThreeHalfHours(@PathVariable String version) {
        log.info("Received request to get top 3 half-hour periods with most cars");
        return ResponseEntity.ok(trafficDataService.getTopThreeHalfHours());
    }

    @ApiVersion(1)
    @GetMapping("/v{version}/traffic/least-cars-period")
    @Operation(summary = "Get contiguous 1.5 hour period with least cars",
              description = "Finds the 1.5-hour period with the lowest total number of cars")
    public ResponseEntity<List<TrafficData>> getLeastCarsPeriod(@PathVariable String version) {
        log.info("Received request to get 1.5 hour period with least cars");
        return ResponseEntity.ok(trafficDataService.findLeastCarsContiguousPeriod());
    }

    @ApiVersion(1)
    @PostMapping("/v{version}/traffic")
    @Operation(summary = "Add new traffic data",
              description = "Adds a new traffic data record with the specified timestamp and car count")
    public ResponseEntity<TrafficData> addTrafficData(
            @PathVariable String version,
            @RequestParam @NotNull(message = "Timestamp is required") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @RequestParam @Min(value = 0, message = "Car count cannot be negative") int carCount) {
        
        log.info("Received request to add traffic data - Timestamp: {}, Car Count: {}", timestamp, carCount);
        TrafficData savedData = trafficDataService.saveTrafficData(timestamp, carCount);
        return ResponseEntity.ok(savedData);
    }

    @ApiVersion(2)
    @GetMapping("/v{version}/traffic/stats")
    @Operation(summary = "Get traffic statistics")
    public ResponseEntity<Map<String, Object>> getTrafficStatsV2(@PathVariable String version) {
        return ResponseEntity.ok(trafficDataService.getTrafficStatistics());
    }
}
