package com.iis.backend.repository;

import com.iis.backend.model.Ticket;
import com.iis.backend.model.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerIdOrderByPurchasedAtDesc(Long customerId);

    boolean existsByMatchIdAndSeatIdAndStatus(Long matchId, Long seatId, TicketStatus status);

    long countByMatchIdAndStatus(Long matchId, TicketStatus status);
}
