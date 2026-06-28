package com.iis.backend.controller;

import com.iis.backend.dto.RecommendationModelRequest;
import com.iis.backend.dto.RecommendationModelResponse;
import com.iis.backend.dto.RecommendationResultResponse;
import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.UserRepository;
import com.iis.backend.service.RecommendationModelService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/recommendation-models")
public class RecommendationModelController {

    private final RecommendationModelService service;
    private final UserRepository userRepository;

    public RecommendationModelController(RecommendationModelService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<RecommendationModelResponse> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecommendationModelResponse create(@Valid @RequestBody RecommendationModelRequest request, Principal principal) {
        return service.create(request, requireDirector(principal));
    }

    @PutMapping("/{id}")
    public RecommendationModelResponse update(@PathVariable Long id, @Valid @RequestBody RecommendationModelRequest request, Principal principal) {
        requireDirector(principal);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal principal) {
        requireDirector(principal);
        service.delete(id);
    }

    @PostMapping("/{id}/calculate")
    public List<RecommendationResultResponse> calculate(@PathVariable Long id, Principal principal) {
        requireDirector(principal);
        return service.calculate(id);
    }

    @GetMapping("/{id}/results")
    public List<RecommendationResultResponse> findResults(@PathVariable Long id) {
        return service.findResults(id);
    }

    private User requireDirector(Principal principal) {
        var user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getRole() != Role.SPORTSKI_DIREKTOR && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo sportski direktor moze da upravlja sistemom preporuke");
        }
        return user;
    }
}
