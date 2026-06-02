package com.iis.backend.repository;

import com.iis.backend.model.Seat;
import com.iis.backend.model.SeatStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByZoneId(Long zoneId);

    List<Seat> findByStatus(SeatStatus status);

    long countByZoneIdAndStatusIn(Long zoneId, List<SeatStatus> statuses);

    boolean existsByZoneIdAndRowLabelIgnoreCaseAndSeatNumber(Long zoneId, String rowLabel, Integer seatNumber);

    boolean existsByZoneIdAndRowLabelIgnoreCaseAndSeatNumberAndIdNot(
            Long zoneId,
            String rowLabel,
            Integer seatNumber,
            Long id);
}
