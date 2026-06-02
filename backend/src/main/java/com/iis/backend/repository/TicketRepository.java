package com.iis.backend.repository;

import com.iis.backend.model.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerIdOrderByPurchasedAtDesc(Long customerId);
}
