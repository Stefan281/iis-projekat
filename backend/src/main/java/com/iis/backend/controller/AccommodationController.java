package com.iis.backend.controller;

import com.iis.backend.dto.AccommodationCreateRequest;
import com.iis.backend.dto.AccommodationDTO;
import com.iis.backend.service.AccommodationService;
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
@RequestMapping("/api/trips/{tripId}/accommodation")
public class AccommodationController {

    private final AccommodationService accommodationService;

    public AccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping
    public ResponseEntity<List<AccommodationDTO>> getOffers(@PathVariable Long tripId) {
        return ResponseEntity.ok(accommodationService.getOffers(tripId));
    }

    @PostMapping
    public ResponseEntity<AccommodationDTO> create(@PathVariable Long tripId,
                                                   @Valid @RequestBody AccommodationCreateRequest request) {
        AccommodationDTO created = accommodationService.create(tripId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/select")
    public ResponseEntity<AccommodationDTO> select(@PathVariable Long tripId,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(accommodationService.select(tripId, id));
    }

    @GetMapping("/selected")
    public ResponseEntity<AccommodationDTO> getSelected(@PathVariable Long tripId) {
        return ResponseEntity.ok(accommodationService.getSelected(tripId));
    }
}
