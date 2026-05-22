package com.iis.backend.repository;

import com.iis.backend.model.MatchEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
    List<MatchEvent> findTop10ByMatchIdOrderByEventTimeDesc(Long matchId);
}
