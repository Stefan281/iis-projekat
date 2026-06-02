package com.iis.backend.controller;

import com.iis.backend.dto.SeatRequest;
import com.iis.backend.dto.SeatResponse;
import com.iis.backend.service.SeatService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public List<SeatResponse> findAll(@RequestParam(required = false) Long zoneId) {
        if (zoneId != null) {
            return seatService.findByZone(zoneId);
        }
        return seatService.findAll();
    }

    @GetMapping("/{id}")
    public SeatResponse findById(@PathVariable Long id) { return seatService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SeatResponse create(@Valid @RequestBody SeatRequest request) { return seatService.create(request); }

    @PutMapping("/{id}")
    public SeatResponse update(@PathVariable Long id, @Valid @RequestBody SeatRequest request) {
        return seatService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { seatService.delete(id); }
}
