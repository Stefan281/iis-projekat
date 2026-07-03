package com.iis.backend.repository;

import com.iis.backend.model.Ticket;
import com.iis.backend.model.TicketStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerIdOrderByPurchasedAtDesc(Long customerId);

    boolean existsByMatchIdAndSeatIdAndStatus(Long matchId, Long seatId, TicketStatus status);

    long countByMatchIdAndStatus(Long matchId, TicketStatus status);

    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.match.id = :matchId AND t.status = :status")
    Optional<BigDecimal> sumRevenueByMatchIdAndStatus(@Param("matchId") Long matchId, @Param("status") TicketStatus status);
}
