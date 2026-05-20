package com.iis.backend.config;

import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(userRepository, passwordEncoder, "admin", "admin123", "Admin", "Korisnik", Role.ADMIN);
            createUserIfMissing(userRepository, passwordEncoder, "statisticar", "statisticar123", "Marko", "Statisticar", Role.STATISTICAR);
            createUserIfMissing(userRepository, passwordEncoder, "stab", "stab123", "Ana", "Strucni stab", Role.STRUCNI_STAB);
        };
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            String firstName,
            String lastName,
            Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        var user = new User(username, passwordEncoder.encode(rawPassword), firstName, lastName, role);
        userRepository.save(user);
    }
}
