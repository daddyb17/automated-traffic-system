package com.example.automatedtrafficsystem.repository;

import com.example.automatedtrafficsystem.model.TrafficData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {

    @Override
    Page<TrafficData> findAll(Pageable pageable);
    
    @Override
    Optional<TrafficData> findById(Long id);
    
    List<TrafficData> findByTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    
    List<TrafficData> findTop3ByOrderByCarCountDesc();

    List<TrafficData> findAllByOrderByTimestampAsc();

    Optional<TrafficData> findByTimestamp(LocalDateTime timestamp);

    boolean existsByTimestamp(LocalDateTime timestamp);

    @Query("SELECT COALESCE(SUM(t.carCount), 0) FROM TrafficData t")
    long getTotalCars();

    @Query(value = """
        SELECT CAST(t.timestamp AS DATE) AS trafficDate, SUM(t.car_count) AS totalCars
        FROM traffic_data t
        GROUP BY CAST(t.timestamp AS DATE)
        ORDER BY CAST(t.timestamp AS DATE)
        """, nativeQuery = true)
    List<DailyTrafficTotalView> findDailyTrafficTotals();

    interface DailyTrafficTotalView {
        String getTrafficDate();
        long getTotalCars();
    }
}
