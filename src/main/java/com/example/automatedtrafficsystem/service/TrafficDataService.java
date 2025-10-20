package com.example.automatedtrafficsystem.service;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrafficDataService {

    private final TrafficDataRepository trafficDataRepository;

    @Transactional
    public TrafficData saveTrafficData(LocalDateTime timestamp, int carCount) {
        TrafficData trafficData = new TrafficData(timestamp, carCount);
        return trafficDataRepository.save(trafficData);
    }

    @Transactional(readOnly = true)
    public int getTotalCars() {
        return trafficDataRepository.findAll().stream()
                .mapToInt(TrafficData::getCarCount)
                .sum();
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> getDailyCarCounts() {
        List<TrafficData> allData = trafficDataRepository.findAll();
        return allData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().toLocalDate(),
                        Collectors.summingInt(TrafficData::getCarCount)
                ));
    }

    @Transactional(readOnly = true)
    public List<TrafficData> getTopThreeHalfHours() {
        return trafficDataRepository.findTopByCarCount(3);
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
