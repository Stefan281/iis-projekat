package com.iis.backend.config;

import com.iis.backend.model.ReservationStatus;
import com.iis.backend.repository.ReservationRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationExpiryScheduler {
    private final ReservationRepository reservationRepository;

    public ReservationExpiryScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void expireReservations() {
        var now = LocalDateTime.now();
        var active = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);
        active.forEach(r -> r.setStatus(ReservationStatus.EXPIRED));
        reservationRepository.saveAll(active);
    }
}
