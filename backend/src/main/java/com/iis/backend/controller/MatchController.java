package com.iis.backend.controller;

import com.iis.backend.dto.MatchRequest;
import com.iis.backend.model.Match;
import com.iis.backend.service.MatchService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<Match> findAll() { return matchService.findAll(); }

    @GetMapping("/{id}")
    public Match findById(@PathVariable Long id) { return matchService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Match create(@Valid @RequestBody MatchRequest request) { return matchService.create(request); }

    @PutMapping("/{id}")
    public Match update(@PathVariable Long id, @Valid @RequestBody MatchRequest request) {
        return matchService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { matchService.delete(id); }
}
