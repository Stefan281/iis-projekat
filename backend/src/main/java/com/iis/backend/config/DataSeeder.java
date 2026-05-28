package com.iis.backend.config;

import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    @Order(0)
    CommandLineRunner refreshRoleConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("alter table users drop constraint if exists users_role_check");
            jdbcTemplate.execute("""
                    alter table users
                    add constraint users_role_check
                    check (role in ('ADMIN', 'SKAUT', 'STATISTICAR', 'STRUCNI_STAB', 'SPORTSKI_DIREKTOR'))
                    """);
        };
    }

    @Bean
    @Order(1)
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(userRepository, passwordEncoder, "admin", "admin@iis.local", "admin123", "Admin", "Korisnik", Role.ADMIN);
            createUserIfMissing(userRepository, passwordEncoder, "skaut", "skaut@iis.local", "skaut123", "Milan", "Skaut", Role.SKAUT);
            createUserIfMissing(userRepository, passwordEncoder, "statisticar", "statisticar@iis.local", "statisticar123", "Marko", "Statisticar", Role.STATISTICAR);
            createUserIfMissing(userRepository, passwordEncoder, "stab", "stab@iis.local", "stab123", "Ana", "Strucni stab", Role.STRUCNI_STAB);
            createUserIfMissing(userRepository, passwordEncoder, "direktor", "direktor@iis.local", "direktor123", "Ivan", "Direktor", Role.SPORTSKI_DIREKTOR);
        };
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String email,
            String rawPassword,
            String firstName,
            String lastName,
            Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        var user = new User(username, email, passwordEncoder.encode(rawPassword), firstName, lastName, role);
        userRepository.save(user);
    }
}
