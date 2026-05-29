package com.iis.backend.service;

import com.iis.backend.dto.CreateMetricRequest;
import com.iis.backend.dto.MetricResponse;
import com.iis.backend.model.Metric;
import com.iis.backend.model.MetricType;
import com.iis.backend.model.User;
import com.iis.backend.repository.MetricRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MetricService {

    private final MetricRepository metricRepository;

    public MetricService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Transactional(readOnly = true)
    public List<MetricResponse> findAll() {
        return metricRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Metric::getId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MetricResponse create(CreateMetricRequest request, User definedBy) {
        metricRepository.findByNameIgnoreCase(request.name().trim()).ifPresent(metric -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metrika sa tim nazivom vec postoji");
        });

        var metric = new Metric(
                request.name().trim(),
                MetricType.OSTALO,
                normalizeOptional(request.description()),
                request.unitOfMeasure().trim(),
                request.standardMetric(),
                definedBy);

        return toResponse(metricRepository.save(metric));
    }

    @Transactional
    public MetricResponse update(Long id, CreateMetricRequest request) {
        var metric = metricRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Metrika nije pronadjena"));
        metricRepository.findByNameIgnoreCase(request.name().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metrika sa tim nazivom vec postoji");
                });

        metric.setName(request.name().trim());
        metric.setStandardMetric(request.standardMetric());
        metric.setUnitOfMeasure(request.unitOfMeasure().trim());
        metric.setDescription(normalizeOptional(request.description()));

        return toResponse(metricRepository.save(metric));
    }

    private MetricResponse toResponse(Metric metric) {
        return new MetricResponse(
                metric.getId(),
                metric.getName(),
                metric.isStandardMetric(),
                metric.getUnitOfMeasure(),
                metric.getDescription());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
