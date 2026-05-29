package com.iis.backend.service;

import com.iis.backend.dto.CreatePlayerRecommendationRequest;
import com.iis.backend.dto.PlayerRecommendationResponse;
import com.iis.backend.model.PlayerAnalysis;
import com.iis.backend.model.PlayerRecommendation;
import com.iis.backend.model.User;
import com.iis.backend.repository.PlayerAnalysisRepository;
import com.iis.backend.repository.PlayerRecommendationRepository;
import com.iis.backend.repository.PlayerRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerRecommendationService {

    private final PlayerRecommendationRepository playerRecommendationRepository;
    private final PlayerRepository playerRepository;
    private final PlayerAnalysisRepository playerAnalysisRepository;

    public PlayerRecommendationService(
            PlayerRecommendationRepository playerRecommendationRepository,
            PlayerRepository playerRepository,
            PlayerAnalysisRepository playerAnalysisRepository) {
        this.playerRecommendationRepository = playerRecommendationRepository;
        this.playerRepository = playerRepository;
        this.playerAnalysisRepository = playerAnalysisRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerRecommendationResponse> findAll() {
        return playerRecommendationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PlayerRecommendation::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlayerRecommendationResponse create(CreatePlayerRecommendationRequest request, User director) {
        var player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));
        var analysis = findAnalysis(request.analysisId());
        var recommendation = new PlayerRecommendation(
                player,
                analysis,
                director,
                request.recommendationDate(),
                normalizeOptional(request.criteria()),
                request.explanation().trim());

        return toResponse(playerRecommendationRepository.save(recommendation));
    }

    @Transactional
    public PlayerRecommendationResponse update(Long id, CreatePlayerRecommendationRequest request, User director) {
        var recommendation = playerRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preporuka nije pronadjena"));
        var player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));

        recommendation.setPlayer(player);
        recommendation.setAnalysis(findAnalysis(request.analysisId()));
        recommendation.setRecommendedBy(director);
        recommendation.setRecommendationDate(request.recommendationDate());
        recommendation.setCriteria(normalizeOptional(request.criteria()));
        recommendation.setExplanation(request.explanation().trim());

        return toResponse(playerRecommendationRepository.save(recommendation));
    }

    @Transactional
    public void delete(Long id) {
        if (!playerRecommendationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Preporuka nije pronadjena");
        }

        playerRecommendationRepository.deleteById(id);
    }

    private PlayerAnalysis findAnalysis(Long analysisId) {
        if (analysisId == null) {
            return null;
        }

        return playerAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analiza nije pronadjena"));
    }

    private PlayerRecommendationResponse toResponse(PlayerRecommendation recommendation) {
        var player = recommendation.getPlayer();
        var analysis = recommendation.getAnalysis();
        var director = recommendation.getRecommendedBy();

        return new PlayerRecommendationResponse(
                recommendation.getId(),
                player.getId(),
                player.getFirstName() + " " + player.getLastName(),
                player.getPosition() == null ? null : player.getPosition().getName(),
                analysis == null ? null : analysis.getId(),
                analysis == null ? null : analysis.getConclusion(),
                recommendation.getRecommendationDate(),
                recommendation.getCriteria(),
                recommendation.getExplanation(),
                director.getFirstName() + " " + director.getLastName());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
