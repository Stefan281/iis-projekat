package com.iis.backend.controller;

import com.iis.backend.dto.TicketTypeRequest;
import com.iis.backend.model.TicketType;
import com.iis.backend.service.TicketTypeService;
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
@RequestMapping("/api/ticket-types")
public class TicketTypeController {
    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @GetMapping
    public List<TicketType> findAll() { return ticketTypeService.findAll(); }

    @GetMapping("/{id}")
    public TicketType findById(@PathVariable Long id) { return ticketTypeService.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketType create(@Valid @RequestBody TicketTypeRequest request) {
        return ticketTypeService.create(request);
    }

    @PutMapping("/{id}")
    public TicketType update(@PathVariable Long id, @Valid @RequestBody TicketTypeRequest request) {
        return ticketTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { ticketTypeService.delete(id); }
}
