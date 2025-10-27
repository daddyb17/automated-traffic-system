package com.example.automatedtrafficsystem.repository;

import com.example.automatedtrafficsystem.model.TrafficData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for managing traffic data operations.
 */
@Repository
public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {
    
    // Basic CRUD operations (inherited from JpaRepository)
    @Override
    Page<TrafficData> findAll(Pageable pageable);
    
    @Override
    Optional<TrafficData> findById(Long id);
    
    // Time-based queries
    @Query("SELECT t FROM TrafficData t WHERE t.timestamp >= :startDate AND t.timestamp < :endDate")
    List<TrafficData> findByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT t FROM TrafficData t WHERE DATE(t.timestamp) = :date")
    List<TrafficData> findByDate(@Param("date") LocalDate date);
    
    // Sorting and limiting queries
    List<TrafficData> findTop3ByOrderByCarCountDesc();
    
    @Query(value = "SELECT * FROM traffic_data ORDER BY car_count DESC LIMIT :limit", nativeQuery = true)
    List<TrafficData> findTopByCarCount(@Param("limit") int limit);
    
    @Query("SELECT t FROM TrafficData t WHERE t.timestamp >= :startDate AND t.timestamp < :endDate " +
           "ORDER BY t.carCount ASC")
    List<TrafficData> findLowestTrafficPeriod(
        @Param("endDate") LocalDateTime endDate
    );
    
    List<TrafficData> findAllByOrderByTimestampAsc();
    
    /**
     * Find traffic data by exact timestamp match.
     *
     * @param timestamp the timestamp to search for
     * @return an Optional containing the TrafficData if found, empty otherwise
     */
    Optional<TrafficData> findByTimestamp(LocalDateTime timestamp);
    
    // Aggregation and reporting queries
    @Query("""
        SELECT NEW map(
            HOUR(t.timestamp) as hour, 
            SUM(t.carCount) as totalCars
        ) 
        FROM TrafficData t 
        GROUP BY HOUR(t.timestamp) 
        ORDER BY SUM(t.carCount) DESC
    """)
    List<Map<String, Object>> findHourlyTrafficDistribution();
    
    @Query("""
        SELECT NEW map(
            DATE(t.timestamp) as date, 
            SUM(t.carCount) as dailyTotal
        ) 
        FROM TrafficData t 
        GROUP BY DATE(t.timestamp) 
        ORDER BY DATE(t.timestamp)
    """)
    List<Map<String, Object>> findDailyTrafficSummary();
}
