package com.iis.backend.controller;

import com.iis.backend.dto.MatchEventRequest;
import com.iis.backend.dto.MatchEventResponse;
import com.iis.backend.dto.MatchResponse;
import com.iis.backend.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/current")
    public MatchResponse getCurrentMatch() {
        return matchService.getCurrentMatch();
    }

    @PostMapping("/{matchId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchEventResponse addEvent(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchEventRequest request) {
        return matchService.addEvent(matchId, request);
    }

    @DeleteMapping("/{matchId}/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long matchId, @PathVariable Long eventId) {
        matchService.deleteEvent(matchId, eventId);
    }
}
