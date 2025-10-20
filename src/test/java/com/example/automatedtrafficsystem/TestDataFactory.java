package com.example.automatedtrafficsystem;

import com.example.automatedtrafficsystem.model.TrafficData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {
    
    public static List<TrafficData> createSampleTrafficData() {
        List<TrafficData> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        
        // Create data for 3 days
        for (int day = 0; day < 3; day++) {
            LocalDateTime dayStart = now.plusDays(day).withHour(0).withMinute(0).withSecond(0);
            
            // Create data points every 30 minutes for 24 hours
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalDateTime timestamp = dayStart
                            .plusHours(hour)
                            .plusMinutes(minute);
                    
                    // Simulate traffic patterns (more during day, less at night)
                    int carCount = 0;
                    if (hour >= 7 && hour <= 9) { // Morning rush
                        carCount = 30 + (int)(Math.random() * 30);
                    } else if (hour >= 16 && hour <= 18) { // Evening rush
                        carCount = 25 + (int)(Math.random() * 35);
                    } else if (hour >= 9 && hour <= 16) { // Daytime
                        carCount = 15 + (int)(Math.random() * 20);
                    } else { // Night
                        carCount = (int)(Math.random() * 10);
                    }
                    
                    TrafficData trafficData = new TrafficData();
                    trafficData.setTimestamp(timestamp);
                    trafficData.setCarCount(carCount);
                    data.add(trafficData);
                }
            }
        }
        
        return data;
    }
    
    public static String createSampleTrafficDataFile() {
        StringBuilder sb = new StringBuilder();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        
        // Create data for 2 days
        for (int day = 0; day < 2; day++) {
            LocalDateTime dayStart = now.plusDays(day);
            
            // Create data points every 30 minutes for 24 hours
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalDateTime timestamp = dayStart
                            .withHour(hour)
                            .withMinute(minute)
                            .withSecond(0);
                    
                    int carCount = (int)(Math.random() * 50);
                    sb.append(timestamp).append(" ").append(carCount).append("\n");
                }
            }
        }
        
        return sb.toString();
    }
}
