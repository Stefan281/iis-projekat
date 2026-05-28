package com.iis.backend.repository;

import com.iis.backend.model.Position;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    boolean existsByName(String name);

    Optional<Position> findByNameIgnoreCase(String name);
}
