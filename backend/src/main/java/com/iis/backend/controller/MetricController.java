package com.iis.backend.controller;

import com.iis.backend.dto.CreateMetricRequest;
import com.iis.backend.dto.MetricResponse;
import com.iis.backend.repository.UserRepository;
import com.iis.backend.service.MetricService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricService metricService;
    private final UserRepository userRepository;

    public MetricController(MetricService metricService, UserRepository userRepository) {
        this.metricService = metricService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<MetricResponse> findAll() {
        return metricService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetricResponse create(@Valid @RequestBody CreateMetricRequest request, Principal principal) {
        var user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return metricService.create(request, user);
    }

    @PutMapping("/{id}")
    public MetricResponse update(@PathVariable Long id, @Valid @RequestBody CreateMetricRequest request) {
        return metricService.update(id, request);
    }
}
