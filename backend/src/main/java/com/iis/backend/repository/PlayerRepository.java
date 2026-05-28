package com.iis.backend.repository;

import com.iis.backend.model.Player;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByClubId(Long clubId);

    List<Player> findByPositionId(Long positionId);

    @EntityGraph(attributePaths = {"club", "position"})
    List<Player> findTop5ByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = {"club", "position"})
    List<Player> findAll();
}
