package com.iis.backend.repository;

import com.iis.backend.model.Club;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByName(String name);

    Optional<Club> findByNameIgnoreCase(String name);
}
