package com.example.automatedtrafficsystem.ai;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
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

    @Nullable
    private final ChatClient chatClient;
    private final TrafficDataRepository trafficDataRepository;
    
    @Value("classpath:/prompts/traffic-analysis-prompt.st")
    private Resource trafficAnalysisPrompt;
    
    public TrafficAnalysisService(
            @Nullable ChatClient chatClient,
            TrafficDataRepository trafficDataRepository) {
        this.chatClient = chatClient;
        this.trafficDataRepository = trafficDataRepository;
    }

    public String analyzeTrafficPatterns(LocalDate startDate, LocalDate endDate) {
        List<TrafficData> trafficData = trafficDataRepository
                .findByTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay()
                );

        if (trafficData.isEmpty()) {
            return "No traffic data available for the specified period.";
        }

        Map<String, Object> context = createAnalysisContext(trafficData, startDate, endDate);

        if (chatClient == null) {
            return "AI analysis is currently unavailable because the OpenAI client is not configured.";
        }

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
        List<TrafficData> historicalData = trafficDataRepository
                .findByTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
                        startTime.minusDays(30),
                        startTime
                );

        if (historicalData.isEmpty()) {
            throw new IllegalStateException("Insufficient historical data for prediction");
        }

        double avgCars = historicalData.stream()
                .mapToInt(TrafficData::getCarCount)
                .average()
                .orElse(0);
                
        int maxCars = historicalData.stream()
                .mapToInt(TrafficData::getCarCount)
                .max()
                .orElse(0);
        
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
        
        int baseTravelTime = 15;
        double trafficFactor = 1.0 + (avgCars / 50.0);
        int expectedTravelTime = (int) (baseTravelTime * trafficFactor);

        return TrafficPrediction.builder()
                .startTime(startTime)
                .endTime(endTime)
                .trafficCondition(trafficCondition)
                .confidenceScore(confidence)
                .averageSpeed(calculateAverageSpeed(avgCars))
                .expectedVolume((int) (avgCars * 0.8))
                .expectedTravelTimeMinutes(expectedTravelTime)
                .details(String.format("Prediction based on %d historical data points", historicalData.size()))
                .build();
    }
    
    private double calculateAverageSpeed(double avgCars) {
        return Math.max(10, 60 - (avgCars * 0.5));
    }

    private Map<String, Object> createAnalysisContext(List<TrafficData> trafficData, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> context = new HashMap<>();

        int totalCars = trafficData.stream().mapToInt(TrafficData::getCarCount).sum();
        double averageCars = trafficData.stream().mapToInt(TrafficData::getCarCount).average().orElse(0);

        String sampleDataStr = trafficData.stream()
                .limit(5)
                .map(data -> String.format("- %s: %d cars", 
                    data.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 
                    data.getCarCount()))
                .collect(Collectors.joining("\n"));

        String dailyAveragesStr = trafficData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().getDayOfWeek().name(),
                        Collectors.averagingInt(TrafficData::getCarCount)))
                .entrySet().stream()
                .map(e -> String.format("- %s: %.1f cars", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));

        String hourlyAveragesStr = trafficData.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getTimestamp().getHour(),
                        Collectors.averagingInt(TrafficData::getCarCount)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("- %02d:00 - %.1f cars", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));

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
}
