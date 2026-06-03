package com.iis.backend.controller;

import com.iis.backend.dto.CreatePlayerAnalysisRequest;
import com.iis.backend.dto.PlayerAnalysisResponse;
import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.UserRepository;
import com.iis.backend.service.PlayerAnalysisService;
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
@RequestMapping("/api/player-analyses")
public class PlayerAnalysisController {

    private final PlayerAnalysisService playerAnalysisService;
    private final UserRepository userRepository;

    public PlayerAnalysisController(PlayerAnalysisService playerAnalysisService, UserRepository userRepository) {
        this.playerAnalysisService = playerAnalysisService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<PlayerAnalysisResponse> findAll() {
        return playerAnalysisService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerAnalysisResponse create(@Valid @RequestBody CreatePlayerAnalysisRequest request, Principal principal) {
        return playerAnalysisService.create(request, requireAnalysisEditor(principal));
    }

    @PutMapping("/{id}")
    public PlayerAnalysisResponse update(@PathVariable Long id, @Valid @RequestBody CreatePlayerAnalysisRequest request, Principal principal) {
        return playerAnalysisService.update(id, request, requireAnalysisEditor(principal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal principal) {
        requireAnalysisEditor(principal);
        playerAnalysisService.delete(id);
    }

    private User requireAnalysisEditor(Principal principal) {
        var user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getRole() != Role.STRUCNI_STAB && user.getRole() != Role.SPORTSKI_DIREKTOR && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo strucni stab ili sportski direktor mogu da uredjuju analize");
        }

        return user;
    }
}
