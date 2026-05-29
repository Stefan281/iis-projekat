package com.iis.backend.controller;

import com.iis.backend.dto.CreateObservationRequest;
import com.iis.backend.dto.ObservationResponse;
import com.iis.backend.repository.UserRepository;
import com.iis.backend.service.ObservationService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/observations")
public class ObservationController {

    private final ObservationService observationService;
    private final UserRepository userRepository;

    public ObservationController(ObservationService observationService, UserRepository userRepository) {
        this.observationService = observationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<ObservationResponse> findAll(@RequestParam(required = false) Long playerId) {
        if (playerId != null) {
            return observationService.findByPlayer(playerId);
        }

        return observationService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ObservationResponse create(@Valid @RequestBody CreateObservationRequest request, Principal principal) {
        var scout = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return observationService.create(request, scout);
    }
}
