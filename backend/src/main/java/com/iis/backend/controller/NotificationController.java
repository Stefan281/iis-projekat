package com.iis.backend.controller;

import com.iis.backend.dto.NotificationRequest;
import com.iis.backend.dto.NotificationResponse;
import com.iis.backend.model.Notification;
import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.NotificationRepository;
import com.iis.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/obavestenja")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository,
                                  UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll() {
        List<NotificationResponse> body = notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request,
                                                       Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User author = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (author.getRole() != Role.ORGANIZATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only an organizer can post notifications");
        }

        Notification n = new Notification();
        n.setText(request.getText());
        n.setCreatedAt(LocalDateTime.now());
        n.setAuthor(author);
        Notification saved = notificationRepository.save(n);

        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationResponse.from(saved));
    }
}
