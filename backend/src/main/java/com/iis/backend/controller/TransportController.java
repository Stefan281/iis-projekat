package com.iis.backend.controller;

import com.iis.backend.dto.TransportCreateRequest;
import com.iis.backend.dto.TransportDTO;
import com.iis.backend.service.TransportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/transport")
public class TransportController {

    private final TransportService transportService;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @GetMapping
    public ResponseEntity<List<TransportDTO>> getOffers(@PathVariable Long tripId) {
        return ResponseEntity.ok(transportService.getOffers(tripId));
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long tripId,
                                    @Valid @RequestBody TransportCreateRequest request) {
        try {
            TransportDTO created = transportService.create(tripId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/select")
    public ResponseEntity<?> select(@PathVariable Long tripId,
                                    @PathVariable Long id) {
        try {
            return ResponseEntity.ok(transportService.select(tripId, id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/selected")
    public ResponseEntity<TransportDTO> getSelected(@PathVariable Long tripId) {
        return ResponseEntity.ok(transportService.getSelected(tripId));
    }
}
