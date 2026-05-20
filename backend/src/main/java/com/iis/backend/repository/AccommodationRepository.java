package com.iis.backend.repository;

import com.iis.backend.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    List<Accommodation> findByTripId(Long tripId);
    Optional<Accommodation> findByTripIdAndSelectedTrue(Long tripId);
}
