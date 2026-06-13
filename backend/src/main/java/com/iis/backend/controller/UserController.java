package com.iis.backend.controller;

import com.iis.backend.dto.UserDTO;
import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> list(@RequestParam(value = "roles", required = false) List<String> roles) {
        List<User> users = new ArrayList<>();
        if (roles == null || roles.isEmpty()) {
            users.addAll(userRepository.findAll());
        } else {
            for (String roleName : roles) {
                try {
                    Role role = Role.valueOf(roleName.trim());
                    users.addAll(userRepository.findByRole(role));
                } catch (IllegalArgumentException ignored) {
                    // skip unknown role names
                }
            }
        }
        List<UserDTO> body = users.stream()
                .sorted(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(User::getLastName, Comparator.nullsLast(String::compareTo)))
                .map(UserDTO::from)
                .toList();
        return ResponseEntity.ok(body);
    }
}
