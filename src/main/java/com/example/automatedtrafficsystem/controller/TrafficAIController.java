package com.example.automatedtrafficsystem.controller;

import com.example.automatedtrafficsystem.ai.TrafficAnalysisService;
import com.example.automatedtrafficsystem.ai.TrafficPrediction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Validated
@RestController
@RequestMapping(
    path = "/api/ai/traffic",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
@Tag(name = "Traffic AI", description = "AI-powered traffic analysis and prediction endpoints")
public class TrafficAIController {

    private final TrafficAnalysisService trafficAnalysisService;

    @GetMapping("/analyze")
    @Operation(summary = "Analyze traffic patterns for a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully analyzed traffic patterns"),
        @ApiResponse(responseCode = "400", description = "Invalid date range provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> analyzeTrafficPatterns(
            @RequestParam @NotNull(message = "Start date is required") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @NotNull(message = "End date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Start date must be before or equal to end date");
        }
        
        log.info("Analyzing traffic patterns from {} to {}", startDate, endDate);
        String analysis = trafficAnalysisService.analyzeTrafficPatterns(startDate, endDate);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/predict")
    @Operation(summary = "Predict traffic for a future time period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully predicted traffic",
                    content = @Content(schema = @Schema(implementation = TrafficPrediction.class))),
        @ApiResponse(responseCode = "400", description = "Invalid time range provided"),
        @ApiResponse(responseCode = "404", description = "Insufficient data for prediction"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TrafficPrediction> predictTraffic(
            @RequestParam @NotNull(message = "Start time is required")
            @FutureOrPresent(message = "Start time must be in the present or future")
            @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull(message = "End time is required")
            @FutureOrPresent(message = "End time must be in the present or future")
            @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (startTime.isAfter(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Start time must be before or equal to end time");
        }
        
        log.info("Predicting traffic from {} to {}", startTime, endTime);
        TrafficPrediction prediction = trafficAnalysisService.predictTraffic(startTime, endTime);
        return ResponseEntity.ok(prediction);
    }
}
