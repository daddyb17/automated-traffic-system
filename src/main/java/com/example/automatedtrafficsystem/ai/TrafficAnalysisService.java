package com.example.automatedtrafficsystem.ai;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrafficAnalysisService {

    private final ChatClient chatClient;
    private final TrafficDataRepository trafficDataRepository;
    private final ObjectMapper objectMapper;
    
    @Value("classpath:/prompts/traffic-analysis-prompt.st")
    private Resource trafficAnalysisPrompt;

    @Value("classpath:/prompts/traffic-prediction-prompt.st")
    private Resource trafficPredictionPrompt;
    
    @Autowired
    public TrafficAnalysisService(
            ChatClient chatClient,
            TrafficDataRepository trafficDataRepository,
            ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.trafficDataRepository = trafficDataRepository;
        this.objectMapper = objectMapper;
    }

    public String analyzeTrafficPatterns(LocalDate startDate, LocalDate endDate) {
        List<TrafficData> trafficData = trafficDataRepository
                .findByTimestampBetween(
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay()
                );

        if (trafficData.isEmpty()) {
            return "No traffic data available for the specified period.";
        }

        Map<String, Object> context = createAnalysisContext(trafficData, startDate, endDate);
        
        try {
            PromptTemplate promptTemplate = new PromptTemplate(trafficAnalysisPrompt);
            Prompt prompt = promptTemplate.create(context);
            return chatClient.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("Error analyzing traffic patterns: {}", e.getMessage(), e);
            return "Unable to analyze traffic patterns at this time. Please try again later.";
        }
    }

    public TrafficPrediction predictTraffic(LocalDateTime startTime, LocalDateTime endTime) {
        // Get historical data (last 30 days)
        List<TrafficData> historicalData = trafficDataRepository
                .findByTimestampBetween(
                        startTime.minusDays(30),
                        startTime
                );

        if (historicalData.isEmpty()) {
            throw new IllegalStateException("Insufficient historical data for prediction");
        }

        // Calculate basic statistics
        double avgCars = historicalData.stream()
                .mapToInt(TrafficData::getCarCount)
                .average()
                .orElse(0);
                
        int maxCars = historicalData.stream()
                .mapToInt(TrafficData::getCarCount)
                .max()
                .orElse(0);
        
        // Simple prediction logic (can be enhanced with ML model)
        String trafficCondition;
        double confidence;
        
        if (avgCars < 10) {
            trafficCondition = "LOW";
            confidence = 0.85;
        } else if (avgCars < 30) {
            trafficCondition = "MODERATE";
            confidence = 0.75;
        } else {
            trafficCondition = "HIGH";
            confidence = 0.80;
        }
        
        // Calculate expected travel time (simplified)
        int baseTravelTime = 15; // minutes
        double trafficFactor = 1.0 + (avgCars / 50.0);
        int expectedTravelTime = (int) (baseTravelTime * trafficFactor);
        
        // Build and return prediction
        return TrafficPrediction.builder()
                .startTime(startTime)
                .endTime(endTime)
                .trafficCondition(trafficCondition)
                .confidenceScore(confidence)
                .averageSpeed(calculateAverageSpeed(avgCars))
                .expectedVolume((int) (avgCars * 0.8)) // 80% of average as prediction
                .expectedTravelTimeMinutes(expectedTravelTime)
                .details(String.format("Prediction based on %d historical data points", historicalData.size()))
                .build();
    }
    
    private double calculateAverageSpeed(double avgCars) {
        // Simple model: speed decreases as traffic increases
        // Base speed of 60 km/h, reducing with traffic
        return Math.max(10, 60 - (avgCars * 0.5));
    }

    private Map<String, Object> createAnalysisContext(List<TrafficData> trafficData, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> context = new HashMap<>();
        
        // Basic statistics
        int totalCars = trafficData.stream().mapToInt(TrafficData::getCarCount).sum();
        double averageCars = trafficData.stream().mapToInt(TrafficData::getCarCount).average().orElse(0);
        
        // Format sample data (first 5 records)
        String sampleDataStr = trafficData.stream()
                .limit(5)
                .map(data -> String.format("- %s: %d cars", 
                    data.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 
                    data.getCarCount()))
                .collect(Collectors.joining("\n"));
        
        // Format daily averages
        String dailyAveragesStr = trafficData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().getDayOfWeek().name(),
                        Collectors.averagingInt(TrafficData::getCarCount)))
                .entrySet().stream()
                .map(e -> String.format("- %s: %.1f cars", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
                
        // Format hourly averages
        String hourlyAveragesStr = trafficData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().getHour(),
                        Collectors.averagingInt(TrafficData::getCarCount)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("- %02d:00 - %.1f cars", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
        
        // Add formatted data to context
        context.put("startDate", startDate);
        context.put("endDate", endDate);
        context.put("totalRecords", trafficData.size());
        context.put("totalCars", totalCars);
        context.put("averageCarsPerInterval", String.format("%.1f", averageCars));
        context.put("sampleData", sampleDataStr);
        context.put("dailyAverages", dailyAveragesStr);
        context.put("hourlyAverages", hourlyAveragesStr);
        
        return context;
    }

    private Map<String, Object> createPredictionContext(List<TrafficData> historicalData, 
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> context = new HashMap<>();
        
        // Prepare historical data for the model
        List<Map<String, Object>> historicalPoints = historicalData.stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", data.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    map.put("carCount", data.getCarCount());
                    map.put("dayOfWeek", data.getTimestamp().getDayOfWeek().name());
                    map.put("hourOfDay", data.getTimestamp().getHour());
                    return map;
                })
                .collect(Collectors.toList());

        context.put("historicalData", historicalPoints);
        context.put("predictionStartTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        context.put("predictionEndTime", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Add metadata about the prediction request
        context.put("predictionRequestTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        context.put("dataPointsCount", historicalData.size());
        
        return context;
    }
}
