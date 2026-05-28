package com.iis.backend.controller;

import com.iis.backend.dto.CreatePlayerRequest;
import com.iis.backend.dto.PlayerResponse;
import com.iis.backend.model.Role;
import com.iis.backend.repository.UserRepository;
import com.iis.backend.service.PlayerService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final UserRepository userRepository;

    public PlayerController(PlayerService playerService, UserRepository userRepository) {
        this.playerService = playerService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<PlayerResponse> findAll() {
        return playerService.findAll();
    }

    @GetMapping("/recent")
    public List<PlayerResponse> findRecent() {
        return playerService.findRecent();
    }

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        return Map.of("players", playerService.count());
    }

    @GetMapping("/{id}")
    public PlayerResponse findById(@PathVariable Long id) {
        return playerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse create(@Valid @RequestBody CreatePlayerRequest request, Principal principal) {
        requireScout(principal);

        return playerService.create(request);
    }

    @PutMapping("/{id}")
    public PlayerResponse update(@PathVariable Long id, @Valid @RequestBody CreatePlayerRequest request, Principal principal) {
        requireScout(principal);

        return playerService.update(id, request);
    }

    private void requireScout(Principal principal) {
        var user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getRole() != Role.SKAUT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo skaut moze da menja igrace");
        }
    }
}
