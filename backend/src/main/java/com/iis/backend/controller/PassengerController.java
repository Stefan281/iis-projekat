package com.iis.backend.controller;

import com.iis.backend.dto.DocumentationUpdateRequest;
import com.iis.backend.dto.PassengerDocumentationRequest;
import com.iis.backend.dto.PassengerRowDTO;
import com.iis.backend.dto.PassengerToggleRequest;
import com.iis.backend.dto.RoomAssignRequest;
import com.iis.backend.service.PassengerService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/trips/{tripId}/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping
    public ResponseEntity<List<PassengerRowDTO>> getPassengers(@PathVariable Long tripId) {
        return ResponseEntity.ok(passengerService.getPassengers(tripId));
    }

    @PostMapping("/toggle")
    public ResponseEntity<PassengerRowDTO> toggle(@PathVariable Long tripId,
                                                  @Valid @RequestBody PassengerToggleRequest request) {
        return ResponseEntity.ok(passengerService.toggle(tripId, request));
    }

    @PutMapping("/{participantId}/room")
    public ResponseEntity<PassengerRowDTO> assignRoom(@PathVariable Long tripId,
                                                      @PathVariable Long participantId,
                                                      @Valid @RequestBody RoomAssignRequest request) {
        return ResponseEntity.ok(passengerService.assignRoom(tripId, participantId, request));
    }

    @PutMapping("/{participantId}/documentation")
    public ResponseEntity<PassengerRowDTO> updateDocumentation(@PathVariable Long tripId,
                                                               @PathVariable Long participantId,
                                                               @Valid @RequestBody DocumentationUpdateRequest request) {
        return ResponseEntity.ok(passengerService.updateDocumentation(tripId, participantId, request));
    }

    @PostMapping("/documentation")
    public ResponseEntity<PassengerRowDTO> setDocumentationForNonParticipant(
            @PathVariable Long tripId,
            @Valid @RequestBody PassengerDocumentationRequest request) {
        return ResponseEntity.ok(passengerService.setDocumentationForNonParticipant(tripId, request));
    }
}
