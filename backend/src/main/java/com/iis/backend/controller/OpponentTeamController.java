package com.iis.backend.controller;

import com.iis.backend.dto.OpponentTeamRequest;
import com.iis.backend.dto.OpponentTeamResponse;
import com.iis.backend.service.OpponentTeamService;
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
@RequestMapping("/api/opponents")
public class OpponentTeamController {

    private final OpponentTeamService opponentTeamService;

    public OpponentTeamController(OpponentTeamService opponentTeamService) {
        this.opponentTeamService = opponentTeamService;
    }

    @GetMapping
    public List<OpponentTeamResponse> getAll() {
        return opponentTeamService.getAll();
    }

    @GetMapping("/{id}")
    public OpponentTeamResponse getById(@PathVariable Long id) {
        return opponentTeamService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpponentTeamResponse create(@Valid @RequestBody OpponentTeamRequest request) {
        return opponentTeamService.create(request);
    }

    @PutMapping("/{id}")
    public OpponentTeamResponse update(@PathVariable Long id, @Valid @RequestBody OpponentTeamRequest request) {
        return opponentTeamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        opponentTeamService.delete(id);
    }
}
