package com.iis.backend.controller;

import com.iis.backend.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Map<String, Object>>> getConversations(Authentication auth) {
        return ResponseEntity.ok(messageService.getConversations(auth));
    }

    @GetMapping("/chat/{otherUserId}")
    public ResponseEntity<List<Map<String, Object>>> getChat(@PathVariable Long otherUserId,
                                                             Authentication auth) {
        return ResponseEntity.ok(messageService.getChat(otherUserId, auth));
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@RequestBody Map<String, Object> body,
                                                    Authentication auth) {
        return ResponseEntity.ok(messageService.send(body, auth));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(Authentication auth) {
        return ResponseEntity.ok(messageService.getAllUsers(auth));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        return ResponseEntity.ok(messageService.getUnreadCount(auth));
    }
}
