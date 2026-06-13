package com.iis.backend.repository;

import com.iis.backend.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    boolean existsByName(String name);
    List<Trip> findByIdNot(Long id);
}
