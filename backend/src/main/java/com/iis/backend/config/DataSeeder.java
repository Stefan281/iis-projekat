package com.iis.backend.config;

import com.iis.backend.enums.TripStatus;
import com.iis.backend.model.Role;
import com.iis.backend.model.Trip;
import com.iis.backend.model.User;
import com.iis.backend.repository.TripRepository;
import com.iis.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    private static final String DEFAULT_PASSWORD = "1234";

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               TripRepository tripRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            seedUsers(userRepository, passwordEncoder);
            seedTrips(tripRepository);
        };
    }

    private void seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        createUserIfMissing(userRepository, passwordEncoder, "organizator", "Marko", "Joknić", Role.ORGANIZATOR);
        createUserIfMissing(userRepository, passwordEncoder, "direktor", "Aleksandar", "Jovančević", Role.GENERALNI_DIREKTOR);
        createUserIfMissing(userRepository, passwordEncoder, "statisticar", "Milan", "Kovačević", Role.STATISTICAR);

        createUserIfMissing(userRepository, passwordEncoder, "trener1", "Marko", "Narančić", Role.STRUCNI_STAB);
        createUserIfMissing(userRepository, passwordEncoder, "trener2", "Siniša", "Gavrančić", Role.STRUCNI_STAB);

        createUserIfMissing(userRepository, passwordEncoder, "igrac1", "Nemanja", "Antunović", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac2", "Vuk", "Nedić", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac3", "Jovan", "Perović", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac4", "Vuk", "Kulpinac", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac5", "Matija", "Milanović", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac6", "Stefan", "Marić", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac7", "Stefan", "Negić", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac8", "Danilo", "Elezović", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac9", "Žarko", "Ubiparip", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac10", "Uroš", "Mišković", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac11", "Branko", "Kopitić", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac12", "Luka", "Stanković", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac13", "Stefan", "Kokeza", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac14", "Andrej", "Aleksić", Role.IGRAC);
        createUserIfMissing(userRepository, passwordEncoder, "igrac15", "Filip", "Savovski", Role.IGRAC);
    }

    private void seedTrips(TripRepository tripRepository) {
        createTripIfMissing(tripRepository,
                "Superliga – Crvena zvezda",
                "Beograd",
                "Utakmica 12. kola Superlige Srbije, gostovanje kod OK Crvena zvezda",
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 6, 6),
                TripStatus.AWAITING_APPROVAL);

        createTripIfMissing(tripRepository,
                "CEV Kup – HOK Budva",
                "Budva, Crna Gora",
                "Prva runda CEV kupa, gostovanje kod HOK Budva",
                LocalDate.of(2026, 6, 14),
                LocalDate.of(2026, 6, 16),
                TripStatus.CONFIRMED);

        createTripIfMissing(tripRepository,
                "Pripremni kamp Zlatibor",
                "Zlatibor",
                "Letnji pripremni kamp pred novu sezonu",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 10),
                TripStatus.CONFIRMED);

        createTripIfMissing(tripRepository,
                "Superliga – OK Ribnica",
                "Kraljevo",
                "Utakmica 18. kola Superlige Srbije, gostovanje kod OK Ribnica",
                LocalDate.of(2026, 7, 22),
                LocalDate.of(2026, 7, 22),
                TripStatus.IN_PROCESSING);

        createTripIfMissing(tripRepository,
                "Prijateljska – OK Partizan",
                "Beograd",
                "Prijateljska utakmica protiv OK Partizan u okviru priprema",
                LocalDate.of(2026, 6, 28),
                LocalDate.of(2026, 6, 28),
                TripStatus.AWAITING_APPROVAL);
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String firstName,
            String lastName,
            Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        var user = new User(username, passwordEncoder.encode(DEFAULT_PASSWORD), firstName, lastName, role);
        user.setEmail(username + "@vojvodina.rs");
        userRepository.save(user);
    }

    private void createTripIfMissing(
            TripRepository tripRepository,
            String name,
            String location,
            String purpose,
            LocalDate departureDate,
            LocalDate returnDate,
            TripStatus status) {
        if (tripRepository.existsByName(name)) {
            return;
        }
        var trip = new Trip();
        trip.setName(name);
        trip.setLocation(location);
        trip.setPurpose(purpose);
        trip.setDepartureDate(departureDate);
        trip.setReturnDate(returnDate);
        trip.setStatus(status);
        tripRepository.save(trip);
    }
}
