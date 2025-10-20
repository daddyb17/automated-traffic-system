package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.service.TrafficDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficDataController {

    private final TrafficDataService trafficDataService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadTrafficData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file");
        }

        try {
            String content = new String(file.getBytes());
            trafficDataService.processTrafficDataFile(content);
            return ResponseEntity.ok("File processed successfully");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error processing file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalCars() {
        return ResponseEntity.ok(trafficDataService.getTotalCars());
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<LocalDate, Integer>> getDailyCarCounts() {
        return ResponseEntity.ok(trafficDataService.getDailyCarCounts());
    }

    @GetMapping("/top-three")
    public ResponseEntity<List<TrafficData>> getTopThreeHalfHours() {
        return ResponseEntity.ok(trafficDataService.getTopThreeHalfHours());
    }

    @GetMapping("/least-cars-period")
    public ResponseEntity<List<TrafficData>> getLeastCarsPeriod() {
        return ResponseEntity.ok(trafficDataService.findLeastCarsContiguousPeriod());
    }

    @PostMapping("/add")
    public ResponseEntity<TrafficData> addTrafficData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @RequestParam int carCount) {
        return ResponseEntity.ok(trafficDataService.saveTrafficData(timestamp, carCount));
    }
}
