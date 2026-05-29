package com.iis.backend.service;

import com.iis.backend.dto.CreatePlayerAnalysisRequest;
import com.iis.backend.dto.PlayerAnalysisResponse;
import com.iis.backend.model.PlayerAnalysis;
import com.iis.backend.model.User;
import com.iis.backend.repository.PlayerAnalysisRepository;
import com.iis.backend.repository.PlayerRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerAnalysisService {

    private final PlayerAnalysisRepository playerAnalysisRepository;
    private final PlayerRepository playerRepository;

    public PlayerAnalysisService(PlayerAnalysisRepository playerAnalysisRepository, PlayerRepository playerRepository) {
        this.playerAnalysisRepository = playerAnalysisRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerAnalysisResponse> findAll() {
        return playerAnalysisRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PlayerAnalysis::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlayerAnalysisResponse create(CreatePlayerAnalysisRequest request, User analyst) {
        var player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));
        var analysis = new PlayerAnalysis(
                player,
                analyst,
                request.analysisDate(),
                request.conclusion().trim(),
                normalizeOptional(request.note()));

        return toResponse(playerAnalysisRepository.save(analysis));
    }

    @Transactional
    public PlayerAnalysisResponse update(Long id, CreatePlayerAnalysisRequest request, User analyst) {
        var analysis = playerAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analiza nije pronadjena"));
        var player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));

        analysis.setPlayer(player);
        analysis.setAnalyst(analyst);
        analysis.setAnalysisDate(request.analysisDate());
        analysis.setConclusion(request.conclusion().trim());
        analysis.setNote(normalizeOptional(request.note()));

        return toResponse(playerAnalysisRepository.save(analysis));
    }

    @Transactional
    public void delete(Long id) {
        if (!playerAnalysisRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analiza nije pronadjena");
        }

        playerAnalysisRepository.deleteById(id);
    }

    private PlayerAnalysisResponse toResponse(PlayerAnalysis analysis) {
        var player = analysis.getPlayer();
        var analyst = analysis.getAnalyst();

        return new PlayerAnalysisResponse(
                analysis.getId(),
                player.getId(),
                player.getFirstName() + " " + player.getLastName(),
                player.getPosition() == null ? null : player.getPosition().getName(),
                analysis.getAnalysisDate(),
                analysis.getConclusion(),
                analysis.getNote(),
                analyst.getFirstName() + " " + analyst.getLastName());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
