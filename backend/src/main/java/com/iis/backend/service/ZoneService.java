package com.iis.backend.service;

import com.iis.backend.dto.ZoneRequest;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.SeatStatus;
import com.iis.backend.model.Zone;
import com.iis.backend.repository.SeatRepository;
import com.iis.backend.repository.ZoneRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final SeatRepository seatRepository;

    public ZoneService(ZoneRepository zoneRepository, SeatRepository seatRepository) {
        this.zoneRepository = zoneRepository;
        this.seatRepository = seatRepository;
    }

    public List<Zone> findAll() {
        return zoneRepository.findAll().stream()
                .map(this::refreshOccupancy)
                .toList();
    }

    public Zone findById(Long id) {
        return refreshOccupancy(zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone was not found")));
    }

    public Zone create(ZoneRequest request) {
        var zone = mapToEntity(new Zone(), request);
        zone.setOccupiedSeats(0);
        zone.setOccupancyRate(BigDecimal.ZERO);
        return zoneRepository.save(zone);
    }

    public Zone update(Long id, ZoneRequest request) {
        return zoneRepository.save(mapToEntity(findById(id), request));
    }

    public void delete(Long id) {
        zoneRepository.delete(findById(id));
    }

    private Zone mapToEntity(Zone zone, ZoneRequest request) {
        zone.setName(request.name());
        zone.setDescription(request.description());
        zone.setPriceCoefficient(request.priceCoefficient());
        zone.setCapacity(request.capacity());
        return zone;
    }

    private Zone refreshOccupancy(Zone zone) {
        if (zone.getId() == null) {
            zone.setOccupiedSeats(0);
            zone.setOccupancyRate(BigDecimal.ZERO);
            return zone;
        }

        var occupied = seatRepository.countByZoneIdAndStatusIn(
                zone.getId(),
                List.of(SeatStatus.RESERVED, SeatStatus.SOLD, SeatStatus.BLOCKED));
        zone.setOccupiedSeats(Math.toIntExact(occupied));

        if (zone.getCapacity() == null || zone.getCapacity() == 0) {
            zone.setOccupancyRate(BigDecimal.ZERO);
            return zone;
        }

        var occupancyRate = BigDecimal.valueOf(occupied)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(zone.getCapacity()), 2, RoundingMode.HALF_UP);
        zone.setOccupancyRate(occupancyRate);
        return zone;
    }
}
