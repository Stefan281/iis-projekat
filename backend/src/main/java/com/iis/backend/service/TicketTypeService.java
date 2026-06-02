package com.iis.backend.service;

import com.iis.backend.dto.TicketTypeRequest;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.TicketType;
import com.iis.backend.repository.TicketTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TicketTypeService {
    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public List<TicketType> findAll() { return ticketTypeRepository.findAll(); }

    public TicketType findById(Long id) {
        return ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type was not found"));
    }

    public TicketType create(TicketTypeRequest request) {
        return ticketTypeRepository.save(mapToEntity(new TicketType(), request));
    }

    public TicketType update(Long id, TicketTypeRequest request) {
        return ticketTypeRepository.save(mapToEntity(findById(id), request));
    }

    public void delete(Long id) {
        ticketTypeRepository.delete(findById(id));
    }

    private TicketType mapToEntity(TicketType ticketType, TicketTypeRequest request) {
        ticketType.setName(request.name());
        ticketType.setDescription(request.description());
        ticketType.setCoefficient(request.coefficient());
        return ticketType;
    }
}
