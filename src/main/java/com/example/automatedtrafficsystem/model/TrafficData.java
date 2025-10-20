package com.example.automatedtrafficsystem.model;

import jakarta.persistence.*;
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

    @Column(name = "timestamp", nullable = false, unique = true)
    private LocalDateTime timestamp;

    @Column(name = "car_count", nullable = false)
    private Integer carCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TrafficData(LocalDateTime timestamp, Integer carCount) {
        this.timestamp = timestamp;
        this.carCount = carCount;
    }
}
