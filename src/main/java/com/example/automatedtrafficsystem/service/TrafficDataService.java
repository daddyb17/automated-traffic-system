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
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficDataService {

    private static final int CONTIGUOUS_PERIOD_RECORDS = 3;
    private final TrafficDataRepository trafficDataRepository;

    @Transactional
    public TrafficData saveTrafficData(LocalDateTime timestamp, int carCount) {
        log.debug("Saving traffic data - Timestamp: {}, Car Count: {}", timestamp, carCount);

        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (carCount < 0) {
            throw new IllegalArgumentException("Car count cannot be negative");
        }
        if (trafficDataRepository.existsByTimestamp(timestamp)) {
            throw new IllegalArgumentException("Traffic data already exists for timestamp: " + timestamp);
        }

        TrafficData trafficData = new TrafficData();
        trafficData.setTimestamp(timestamp);
        trafficData.setCarCount(carCount);

        return trafficDataRepository.save(trafficData);
    }

    @Transactional(readOnly = true)
    public long getTotalCars() {
        log.debug("Calculating total number of cars");
        return trafficDataRepository.getTotalCars();
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Long> getDailyCarCounts() {
        log.debug("Retrieving daily car counts");

        TreeMap<LocalDate, Long> result = new TreeMap<>();
        for (TrafficDataRepository.DailyTrafficTotalView row : trafficDataRepository.findDailyTrafficTotals()) {
            result.put(LocalDate.parse(row.getTrafficDate()), row.getTotalCars());
        }
        return result;
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
        long totalCars = trafficDataRepository.getTotalCars();
        double averageCarsPerDay = getDailyCarCounts().values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        Map<Integer, Integer> hourlyDistribution = allData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().getHour(),
                        Collectors.summingInt(TrafficData::getCarCount)
                ));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCars", totalCars);
        stats.put("averageCarsPerDay", averageCarsPerDay);
        stats.put("totalRecords", allData.size());

        hourlyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(peakHour -> {
                    stats.put("peakHour", String.format("%02d:00 - %02d:59", peakHour.getKey(), peakHour.getKey()));
                    stats.put("carsInPeakHour", peakHour.getValue());
                });

        return stats;
    }

    @Transactional(readOnly = true)
    public List<TrafficData> findLeastCarsContiguousPeriod() {
        List<TrafficData> allData = trafficDataRepository.findAllByOrderByTimestampAsc();
        if (allData.size() < CONTIGUOUS_PERIOD_RECORDS) {
            return List.copyOf(allData);
        }

        int minSum = Integer.MAX_VALUE;
        List<TrafficData> minWindow = List.of();

        for (int i = 0; i <= allData.size() - CONTIGUOUS_PERIOD_RECORDS; i++) {
            if (!isThirtyMinuteContiguousWindow(allData, i)) {
                continue;
            }

            int sum = allData.get(i).getCarCount()
                    + allData.get(i + 1).getCarCount()
                    + allData.get(i + 2).getCarCount();

            if (sum < minSum) {
                minSum = sum;
                minWindow = allData.subList(i, i + CONTIGUOUS_PERIOD_RECORDS);
            }
        }

        return minWindow;
    }

    @Transactional
    public void processTrafficDataFile(String fileContent) {
        if (fileContent == null || fileContent.isBlank()) {
            throw new IllegalArgumentException("File content cannot be empty");
        }

        String[] lines = fileContent.split("\\r?\\n");
        Set<LocalDateTime> fileTimestamps = new HashSet<>();
        List<TrafficData> recordsToSave = new ArrayList<>();

        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format at line " + (index + 1) + ". Expected: <timestamp> <carCount>");
            }

            LocalDateTime timestamp;
            try {
                timestamp = LocalDateTime.parse(parts[0]);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid timestamp at line " + (index + 1) + ": " + parts[0]);
            }

            int carCount;
            try {
                carCount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid car count at line " + (index + 1) + ": " + parts[1]);
            }

            if (carCount < 0) {
                throw new IllegalArgumentException("Car count cannot be negative at line " + (index + 1));
            }
            if (!fileTimestamps.add(timestamp)) {
                throw new IllegalArgumentException("Duplicate timestamp found in file at line " + (index + 1) + ": " + timestamp);
            }
            if (trafficDataRepository.existsByTimestamp(timestamp)) {
                throw new IllegalArgumentException("Traffic data already exists for timestamp: " + timestamp);
            }

            TrafficData trafficData = new TrafficData();
            trafficData.setTimestamp(timestamp);
            trafficData.setCarCount(carCount);
            recordsToSave.add(trafficData);
        }

        if (recordsToSave.isEmpty()) {
            throw new IllegalArgumentException("No valid traffic records found in uploaded file");
        }

        trafficDataRepository.saveAll(recordsToSave);
    }

    private boolean isThirtyMinuteContiguousWindow(List<TrafficData> allData, int start) {
        return allData.get(start).getTimestamp().plusMinutes(30).equals(allData.get(start + 1).getTimestamp())
                && allData.get(start + 1).getTimestamp().plusMinutes(30).equals(allData.get(start + 2).getTimestamp());
    }
}
