package com.example.automatedtrafficsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false, unique = true)
    private LocalDateTime timestamp;

    @NotNull(message = "Car count is required")
    @Min(value = 0, message = "Car count cannot be negative")
    @Column(name = "car_count", nullable = false)
    private Integer carCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TrafficData(LocalDateTime timestamp, Integer carCount) {
        this.timestamp = timestamp;
        this.carCount = carCount;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
