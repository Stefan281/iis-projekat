package com.iis.backend.controller;

import com.iis.backend.dto.ZoneRequest;
import com.iis.backend.model.Zone;
import com.iis.backend.service.ZoneService;
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
@RequestMapping("/api/zones")
public class ZoneController {
    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public List<Zone> findAll() { return zoneService.findAll(); }

    @GetMapping("/{id}")
    public Zone findById(@PathVariable Long id) { return zoneService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Zone create(@Valid @RequestBody ZoneRequest request) { return zoneService.create(request); }

    @PutMapping("/{id}")
    public Zone update(@PathVariable Long id, @Valid @RequestBody ZoneRequest request) {
        return zoneService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { zoneService.delete(id); }
}
