package com.iis.backend.controller;

import com.iis.backend.dto.PurchaseRequest;
import com.iis.backend.dto.TicketResponse;
import com.iis.backend.service.TicketService;
import com.iis.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final UserService userService;

    public TicketController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping("/my")
    public List<TicketResponse> findMyTickets(Authentication authentication) {
        return ticketService.findByCustomer(userService.getCurrentUser(authentication));
    }

    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TicketResponse> purchase(Authentication authentication, @Valid @RequestBody PurchaseRequest request) {
        return ticketService.purchase(userService.getCurrentUser(authentication), request);
    }
}
