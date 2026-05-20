package com.iis.backend.repository;

import com.iis.backend.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {
    List<Transport> findByTripId(Long tripId);
    Optional<Transport> findByTripIdAndSelectedTrue(Long tripId);
}
