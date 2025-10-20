package com.example.automatedtrafficsystem.repository;

import com.example.automatedtrafficsystem.model.TrafficData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {
    
    List<TrafficData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT t FROM TrafficData t WHERE DATE(t.timestamp) = :date")
    List<TrafficData> findByDate(@Param("date") LocalDate date);
    
    @Query(value = "SELECT * FROM traffic_data ORDER BY car_count DESC LIMIT :limit", nativeQuery = true)
    List<TrafficData> findTopByCarCount(@Param("limit") int limit);
    
    @Query("SELECT t FROM TrafficData t WHERE t.timestamp >= :startDate AND t.timestamp < :endDate" +
           " ORDER BY t.carCount ASC")
    List<TrafficData> findLowestTrafficPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    List<TrafficData> findAllByOrderByTimestampAsc();
}
