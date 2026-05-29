package com.iis.backend.controller;

import com.iis.backend.dto.CreatePerformanceRequest;
import com.iis.backend.dto.PerformanceResponse;
import com.iis.backend.repository.UserRepository;
import com.iis.backend.service.PerformanceService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final UserRepository userRepository;

    public PerformanceController(PerformanceService performanceService, UserRepository userRepository) {
        this.performanceService = performanceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<PerformanceResponse> findAll(@RequestParam(required = false) Long playerId) {
        if (playerId != null) {
            return performanceService.findByPlayer(playerId);
        }

        return performanceService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerformanceResponse create(@Valid @RequestBody CreatePerformanceRequest request, Principal principal) {
        var user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return performanceService.create(request, user);
    }

    @PutMapping("/{id}")
    public PerformanceResponse update(@PathVariable Long id, @Valid @RequestBody CreatePerformanceRequest request) {
        return performanceService.update(id, request);
    }
}
