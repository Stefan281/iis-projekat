package com.iis.backend.service;

import com.iis.backend.dto.CreateObservationRequest;
import com.iis.backend.dto.ObservationResponse;
import com.iis.backend.model.Observation;
import com.iis.backend.model.User;
import com.iis.backend.repository.ObservationRepository;
import com.iis.backend.repository.PlayerRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ObservationService {

    private final ObservationRepository observationRepository;
    private final PlayerRepository playerRepository;

    public ObservationService(ObservationRepository observationRepository, PlayerRepository playerRepository) {
        this.observationRepository = observationRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> findAll() {
        return observationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Observation::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ObservationResponse> findByPlayer(Long playerId) {
        return observationRepository.findByPlayerId(playerId)
                .stream()
                .sorted(Comparator.comparing(Observation::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ObservationResponse create(CreateObservationRequest request, User scout) {
        var player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));
        var observation = new Observation(
                player,
                scout,
                request.observationDate(),
                request.period().trim(),
                normalizeOptional(request.note()));

        return toResponse(observationRepository.save(observation));
    }

    private ObservationResponse toResponse(Observation observation) {
        var player = observation.getPlayer();

        return new ObservationResponse(
                observation.getId(),
                player.getId(),
                player.getFirstName() + " " + player.getLastName(),
                player.getPosition() == null ? null : player.getPosition().getName(),
                observation.getDateFrom(),
                observation.getObservationPeriod(),
                observation.getNote());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
