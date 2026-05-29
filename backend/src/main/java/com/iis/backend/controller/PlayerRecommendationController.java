package com.iis.backend.controller;

import com.iis.backend.dto.CreatePlayerRecommendationRequest;
import com.iis.backend.dto.PlayerRecommendationResponse;
import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.UserRepository;
import com.iis.backend.service.PlayerRecommendationService;
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
@RequestMapping("/api/player-recommendations")
public class PlayerRecommendationController {

    private final PlayerRecommendationService playerRecommendationService;
    private final UserRepository userRepository;

    public PlayerRecommendationController(PlayerRecommendationService playerRecommendationService, UserRepository userRepository) {
        this.playerRecommendationService = playerRecommendationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<PlayerRecommendationResponse> findAll() {
        return playerRecommendationService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerRecommendationResponse create(@Valid @RequestBody CreatePlayerRecommendationRequest request, Principal principal) {
        return playerRecommendationService.create(request, requireDirector(principal));
    }

    @PutMapping("/{id}")
    public PlayerRecommendationResponse update(@PathVariable Long id, @Valid @RequestBody CreatePlayerRecommendationRequest request, Principal principal) {
        return playerRecommendationService.update(id, request, requireDirector(principal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal principal) {
        requireDirector(principal);
        playerRecommendationService.delete(id);
    }

    private User requireDirector(Principal principal) {
        var user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getRole() != Role.SPORTSKI_DIREKTOR && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo sportski direktor moze da uredjuje preporuke");
        }

        return user;
    }
}
