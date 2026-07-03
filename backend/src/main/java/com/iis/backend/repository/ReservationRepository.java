package com.iis.backend.repository;

import com.iis.backend.model.Reservation;
import com.iis.backend.model.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime dateTime);

    boolean existsByMatchIdAndSeatIdAndStatus(Long matchId, Long seatId, ReservationStatus status);

    Optional<Reservation> findByMatchIdAndSeatIdAndStatus(Long matchId, Long seatId, ReservationStatus status);

    long countByMatchIdAndStatus(Long matchId, ReservationStatus status);
}
