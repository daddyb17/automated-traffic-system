package com.example.automatedtrafficsystem.service;

import com.example.automatedtrafficsystem.model.TrafficData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrafficReportService {

    private final TrafficDataService trafficDataService;

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        
        // 1. Total number of cars seen
        int totalCars = trafficDataService.getTotalCars();
        report.append("Total cars seen: ").append(totalCars).append("\n\n");
        
        // 2. Daily car counts
        report.append("Daily car counts:\n");
        Map<LocalDate, Integer> dailyCounts = trafficDataService.getDailyCarCounts();
        dailyCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> 
                    report.append(entry.getKey())
                          .append(" ")
                          .append(entry.getValue())
                          .append("\n")
                );
        
        // 3. Top 3 half hours with most cars
        report.append("\nTop 3 half hours with most cars:\n");
        List<TrafficData> topThree = trafficDataService.getTopThreeHalfHours();
        for (TrafficData data : topThree) {
            report.append(data.getTimestamp())
                  .append(" ")
                  .append(data.getCarCount())
                  .append("\n");
        }
        
        // 4. 1.5 hour period with least cars
        report.append("\n1.5 hour period with least cars (3 contiguous half-hour records):\n");
        List<TrafficData> leastCarsPeriod = trafficDataService.findLeastCarsContiguousPeriod();
        for (TrafficData data : leastCarsPeriod) {
            report.append(data.getTimestamp())
                  .append(" ")
                  .append(data.getCarCount())
                  .append("\n");
        }
        
        return report.toString();
    }
    
    public Map<String, Object> getReportAsJson() {
        Map<String, Object> report = new TreeMap<>();
        
        // 1. Total number of cars seen
        report.put("totalCars", trafficDataService.getTotalCars());
        
        // 2. Daily car counts
        Map<LocalDate, Integer> dailyCounts = trafficDataService.getDailyCarCounts();
        report.put("dailyCounts", new TreeMap<>(dailyCounts));
        
        // 3. Top 3 half hours with most cars
        List<Map<String, Object>> topThree = trafficDataService.getTopThreeHalfHours().stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", data.getTimestamp().toString());
                    map.put("carCount", data.getCarCount());
                    return map;
                })
                .collect(Collectors.toList());
        report.put("topThreeHalfHours", topThree);
        
        // 4. 1.5 hour period with least cars
        List<Map<String, Object>> leastCarsPeriod = trafficDataService.findLeastCarsContiguousPeriod().stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", data.getTimestamp().toString());
                    map.put("carCount", data.getCarCount());
                    return map;
                })
                .collect(Collectors.toList());
        report.put("leastCarsPeriod", leastCarsPeriod);
        
        return report;
    }
}
