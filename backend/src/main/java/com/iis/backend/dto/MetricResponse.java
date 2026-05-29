package com.iis.backend.dto;

public record MetricResponse(
        Long id,
        String name,
        boolean standardMetric,
        String unitOfMeasure,
        String description) {
}
