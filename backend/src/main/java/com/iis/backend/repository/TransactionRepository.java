package com.iis.backend.repository;

import com.iis.backend.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
