package com.iis.backend.repository;

import com.iis.backend.model.Match;
import com.iis.backend.model.MatchStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findFirstByStatusOrderByMatchDateDesc(MatchStatus status);
}
