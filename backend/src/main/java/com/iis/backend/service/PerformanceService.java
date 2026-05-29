package com.iis.backend.service;

import com.iis.backend.dto.CreatePerformanceRequest;
import com.iis.backend.dto.PerformanceResponse;
import com.iis.backend.model.PerformanceValue;
import com.iis.backend.model.User;
import com.iis.backend.repository.MetricRepository;
import com.iis.backend.repository.PerformanceValueRepository;
import com.iis.backend.repository.PlayerRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerformanceService {

    private final PerformanceValueRepository performanceValueRepository;
    private final PlayerRepository playerRepository;
    private final MetricRepository metricRepository;

    public PerformanceService(
            PerformanceValueRepository performanceValueRepository,
            PlayerRepository playerRepository,
            MetricRepository metricRepository) {
        this.performanceValueRepository = performanceValueRepository;
        this.playerRepository = playerRepository;
        this.metricRepository = metricRepository;
    }

    @Transactional(readOnly = true)
    public List<PerformanceResponse> findAll() {
        return performanceValueRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PerformanceValue::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PerformanceResponse> findByPlayer(Long playerId) {
        return performanceValueRepository.findByPlayerId(playerId)
                .stream()
                .sorted(Comparator.comparing(PerformanceValue::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PerformanceResponse create(CreatePerformanceRequest request, User recordedBy) {
        var player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));
        var metric = metricRepository.findById(request.metricId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Metrika nije pronadjena"));

        var performance = new PerformanceValue(
                player,
                metric,
                null,
                recordedBy,
                request.value(),
                normalizeOptional(request.comment()),
                LocalDateTime.now());

        return toResponse(performanceValueRepository.save(performance));
    }

    @Transactional
    public PerformanceResponse update(Long id, CreatePerformanceRequest request) {
        var performance = performanceValueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performansa nije pronadjena"));
        var player = playerRepository.findById(request.playerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));
        var metric = metricRepository.findById(request.metricId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Metrika nije pronadjena"));

        performance.setPlayer(player);
        performance.setMetric(metric);
        performance.setValue(request.value());
        performance.setComment(normalizeOptional(request.comment()));

        return toResponse(performanceValueRepository.save(performance));
    }

    private PerformanceResponse toResponse(PerformanceValue performanceValue) {
        var player = performanceValue.getPlayer();
        var metric = performanceValue.getMetric();

        return new PerformanceResponse(
                performanceValue.getId(),
                player.getId(),
                player.getFirstName() + " " + player.getLastName(),
                player.getPosition() == null ? null : player.getPosition().getName(),
                metric.getId(),
                metric.getName(),
                metric.getUnitOfMeasure(),
                performanceValue.getValue(),
                performanceValue.getComment(),
                performanceValue.getRecordedAt());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
