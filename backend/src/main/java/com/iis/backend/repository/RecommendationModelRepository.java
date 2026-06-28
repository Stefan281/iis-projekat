package com.iis.backend.repository;

import com.iis.backend.model.RecommendationModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationModelRepository extends JpaRepository<RecommendationModel, Long> {

    @Override
    @EntityGraph(attributePaths = {"createdBy", "criteria", "criteria.metric"})
    List<RecommendationModel> findAll();

    @Override
    @EntityGraph(attributePaths = {"createdBy", "criteria", "criteria.metric"})
    Optional<RecommendationModel> findById(Long id);
}
