package com.iis.backend.repository;

import com.iis.backend.model.BehaviorComparison;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BehaviorComparisonRepository extends JpaRepository<BehaviorComparison, Long> {

    List<BehaviorComparison> findByFirstPlayerIdOrSecondPlayerId(Long firstPlayerId, Long secondPlayerId);

    List<BehaviorComparison> findByComparedById(Long comparedById);
}
