package com.example.automatedtrafficsystem.service;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficDataService {

    private final TrafficDataRepository trafficDataRepository;

    @Transactional
    public TrafficData saveTrafficData(LocalDateTime timestamp, int carCount) {
        log.debug("Saving traffic data - Timestamp: {}, Car Count: {}", timestamp, carCount);
        
        // Input validation
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (carCount < 0) {
            throw new IllegalArgumentException("Car count cannot be negative");
        }
        
        // Check for existing data at the same timestamp
        trafficDataRepository.findByTimestamp(timestamp).ifPresent(data -> {
            throw new IllegalArgumentException("Traffic data already exists for timestamp: " + timestamp);
        });
        
        TrafficData trafficData = new TrafficData();
        trafficData.setTimestamp(timestamp);
        trafficData.setCarCount(carCount);
        
        return trafficDataRepository.save(trafficData);
    }

    @Transactional(readOnly = true)
    public int getTotalCars() {
        log.debug("Calculating total number of cars");
        List<TrafficData> allData = trafficDataRepository.findAll();
        
        if (allData == null || allData.isEmpty()) {
            log.info("No traffic data found");
            return 0;
        }
        
        return allData.stream()
                .filter(Objects::nonNull)
                .mapToInt(data -> data.getCarCount() != null ? data.getCarCount() : 0)
                .sum();
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> getDailyCarCounts() {
        log.debug("Retrieving daily car counts");
        List<TrafficData> allData = trafficDataRepository.findAll();
        
        if (allData == null || allData.isEmpty()) {
            log.info("No traffic data found for daily counts");
            return Collections.emptyMap();
        }
        
        return allData.stream()
                .filter(Objects::nonNull)
                .filter(data -> data.getTimestamp() != null && data.getCarCount() != null)
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().toLocalDate(),
                        TreeMap::new,
                        Collectors.summingInt(TrafficData::getCarCount)
                ));
    }

    @Transactional(readOnly = true)
    public List<TrafficData> getTopThreeHalfHours() {
        log.debug("Retrieving top three half-hour periods with most cars");
        return trafficDataRepository.findTop3ByOrderByCarCountDesc();
    }
    
    @Transactional(readOnly = true)
    public Optional<TrafficData> getTrafficDataById(Long id) {
        log.debug("Retrieving traffic data by id: {}", id);
        return trafficDataRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Page<TrafficData> getAllTrafficData(Pageable pageable) {
        log.debug("Retrieving paginated traffic data - Page: {}, Size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return trafficDataRepository.findAll(pageable);
    }
    
    
    @Transactional(readOnly = true)
    public Map<String, Object> getTrafficStatistics() {
        log.debug("Generating traffic statistics");
        List<TrafficData> allData = trafficDataRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Basic statistics
        int totalCars = allData.stream().mapToInt(TrafficData::getCarCount).sum();
        double avgCarsPerDay = allData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().toLocalDate(),
                        Collectors.summingInt(TrafficData::getCarCount)
                ))
                .values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
                
        // Peak hours analysis
        Map<Integer, Integer> hourlyDistribution = allData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().getHour(),
                        Collectors.summingInt(TrafficData::getCarCount)
                ));
        
        // Find peak hour
        Map.Entry<Integer, Integer> peakHour = hourlyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        stats.put("totalCars", totalCars);
        stats.put("averageCarsPerDay", String.format("%.2f", avgCarsPerDay));
        stats.put("totalRecords", allData.size());
        
        if (peakHour != null) {
            stats.put("peakHour", String.format("%02d:00 - %02:59", peakHour.getKey(), peakHour.getKey()));
            stats.put("carsInPeakHour", peakHour.getValue());
        }
        
        return stats;
    }

    @Transactional(readOnly = true)
    public List<TrafficData> findLeastCarsContiguousPeriod() {
        List<TrafficData> allData = trafficDataRepository.findAllByOrderByTimestampAsc();
        if (allData.size() < 3) {
            return allData;
        }

        int minSum = Integer.MAX_VALUE;
        int minIndex = 0;

        for (int i = 0; i <= allData.size() - 3; i++) {
            int sum = allData.get(i).getCarCount() + 
                     allData.get(i + 1).getCarCount() + 
                     allData.get(i + 2).getCarCount();
            
            if (sum < minSum) {
                minSum = sum;
                minIndex = i;
            }
        }

        return allData.subList(minIndex, minIndex + 3);
    }

    @Transactional
    public void processTrafficDataFile(String fileContent) {
        String[] lines = fileContent.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.trim().split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid line format: " + line);
            }
            
            LocalDateTime timestamp = LocalDateTime.parse(parts[0]);
            int carCount = Integer.parseInt(parts[1]);
            
            saveTrafficData(timestamp, carCount);
        }
    }
}
