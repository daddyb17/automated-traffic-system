package com.example.automatedtrafficsystem.service;

import com.example.automatedtrafficsystem.model.TrafficData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        long totalCars = trafficDataService.getTotalCars();
        report.append("Total cars seen: ").append(totalCars).append("\n\n");

        report.append("Daily car counts:\n");
        Map<LocalDate, Long> dailyCounts = trafficDataService.getDailyCarCounts();
        dailyCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> 
                    report.append(entry.getKey())
                          .append(" ")
                          .append(entry.getValue())
                          .append("\n")
                );

        report.append("\nTop 3 half hours with most cars:\n");
        List<TrafficData> topThree = trafficDataService.getTopThreeHalfHours();
        for (TrafficData data : topThree) {
            report.append(data.getTimestamp())
                  .append(" ")
                  .append(data.getCarCount())
                  .append("\n");
        }

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

        report.put("totalCars", trafficDataService.getTotalCars());

        Map<LocalDate, Long> dailyCounts = trafficDataService.getDailyCarCounts();
        report.put("dailyCounts", new TreeMap<>(dailyCounts));

        List<Map<String, Object>> topThree = trafficDataService.getTopThreeHalfHours().stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", data.getTimestamp().toString());
                    map.put("carCount", data.getCarCount());
                    return map;
                })
                .collect(Collectors.toList());
        report.put("topThreeHalfHours", topThree);

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
