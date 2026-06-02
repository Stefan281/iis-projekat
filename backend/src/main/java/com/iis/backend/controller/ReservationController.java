package com.iis.backend.controller;

import com.iis.backend.dto.ReservationRequest;
import com.iis.backend.dto.ReservationResponse;
import com.iis.backend.service.ReservationService;
import com.iis.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import com.iis.backend.model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @GetMapping("/my")
    public List<ReservationResponse> findMyReservations(Authentication authentication) {
        return reservationService.findByCustomer(userService.getCurrentUser(authentication));
    }

    @GetMapping
    public List<ReservationResponse> findReservations(Authentication authentication) {
        var user = userService.getCurrentUser(authentication);
        if (user.getRole() == Role.CUSTOMER) {
            return reservationService.findByCustomer(user);
        }
        return reservationService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(Authentication authentication, @Valid @RequestBody ReservationRequest request) {
        return reservationService.reserve(userService.getCurrentUser(authentication), request);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(Authentication authentication, @PathVariable Long id) {
        return reservationService.cancel(userService.getCurrentUser(authentication), id);
    }
}
