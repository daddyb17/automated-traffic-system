package com.example.automatedtrafficsystem.config;

import com.example.automatedtrafficsystem.model.TrafficData;
import com.example.automatedtrafficsystem.repository.TrafficDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final TrafficDataRepository trafficDataRepository;

    @Bean
    public CommandLineRunner initSampleData() {
        return args -> {
            if (trafficDataRepository.count() == 0) {
                log.info("Loading sample traffic data...");
                try {
                    ClassPathResource resource = new ClassPathResource("sample-data.txt");
                    String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                    
                    String[] lines = content.split("\r?\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;
                        
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length == 2) {
                            LocalDateTime timestamp = LocalDateTime.parse(parts[0], DATE_TIME_FORMATTER);
                            int carCount = Integer.parseInt(parts[1]);
                            
                            TrafficData data = new TrafficData(timestamp, carCount);
                            trafficDataRepository.save(data);
                        }
                    }
                    log.info("Loaded {} traffic data records", trafficDataRepository.count());
                } catch (IOException e) {
                    log.warn("Could not load sample data: {}", e.getMessage());
                }
            }
        };
    }
}
