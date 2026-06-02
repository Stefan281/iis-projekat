package com.iis.backend.repository;

import com.iis.backend.model.Reservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
