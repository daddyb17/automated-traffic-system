package com.example.automatedtrafficsystem.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents the prediction result for traffic conditions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficPrediction {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String trafficCondition;
    private double confidenceScore;
    private String details;
    private Double averageSpeed;
    private Integer expectedVolume;
    private String potentialIncidents;
    private String alternativeRoutes;
    private Integer expectedTravelTimeMinutes;
}
