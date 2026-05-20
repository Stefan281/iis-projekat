package com.iis.backend.controller;

import com.iis.backend.dto.PutovanjeRequest;
import com.iis.backend.dto.PutovanjeResponse;
import com.iis.backend.service.PutovanjeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/putovanja")
public class PutovanjeController {

    private final PutovanjeService putovanjeService;

    public PutovanjeController(PutovanjeService putovanjeService) {
        this.putovanjeService = putovanjeService;
    }

    @GetMapping
    public ResponseEntity<List<PutovanjeResponse>> getAll() {
        return ResponseEntity.ok(putovanjeService.getAllPutovanja());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PutovanjeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(putovanjeService.getPutovanjeById(id));
    }

    @PostMapping
    public ResponseEntity<PutovanjeResponse> create(@Valid @RequestBody PutovanjeRequest request) {
        PutovanjeResponse created = putovanjeService.createPutovanje(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PutovanjeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody PutovanjeRequest request) {
        return ResponseEntity.ok(putovanjeService.updatePutovanje(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        putovanjeService.deletePutovanje(id);
        return ResponseEntity.noContent().build();
    }
}
