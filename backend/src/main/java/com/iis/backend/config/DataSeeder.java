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
            createUserIfMissing(userRepository, passwordEncoder, "menadzer", "menadzer123", "Petar", "Petrovic", Role.MANAGER);
            createUserIfMissing(userRepository, passwordEncoder, "statisticar", "statisticar123", "Marko", "Statisticar", Role.STATISTICAR);
            createUserIfMissing(userRepository, passwordEncoder, "stab", "stab123", "Ana", "Strucni stab", Role.STRUCNI_STAB);
            createCustomerIfMissing(userRepository, passwordEncoder);
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

    private void createCustomerIfMissing(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        var email = "customer@example.com";
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(email)) {
            return;
        }

        var user = new User(email, passwordEncoder.encode("customer123"), "Demo", "Customer", Role.CUSTOMER);
        user.setEmail(email);
        user.setCountry("Serbia");
        user.setCity("Novi Sad");
        user.setStreet("Bulevar oslobodjenja");
        user.setStreetNumber("1");
        user.setPostalCode("21000");
        user.setPhoneNumber("+38160111222");
        userRepository.save(user);
    }
}
