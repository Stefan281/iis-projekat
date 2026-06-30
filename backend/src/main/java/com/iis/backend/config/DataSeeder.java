package com.iis.backend.config;

import com.iis.backend.enums.TripStatus;
import com.iis.backend.model.Accommodation;
import com.iis.backend.model.Role;
import com.iis.backend.model.Transport;
import com.iis.backend.model.Trip;
import com.iis.backend.model.User;
import com.iis.backend.repository.AccommodationRepository;
import com.iis.backend.repository.TransportRepository;
import com.iis.backend.repository.TripRepository;
import com.iis.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Configuration
public class DataSeeder {

    private static final String DEFAULT_PASSWORD = "1234";

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               TripRepository tripRepository,
                               AccommodationRepository accommodationRepository,
                               TransportRepository transportRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            seedUsers(userRepository, passwordEncoder);
            seedTrips(tripRepository);
            seedOffers(tripRepository, accommodationRepository, transportRepository);
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

    private void seedOffers(TripRepository tripRepository,
                            AccommodationRepository accommodationRepository,
                            TransportRepository transportRepository) {
        Trip crvenaZvezda = findTripByName(tripRepository, "Superliga – Crvena zvezda");
        if (crvenaZvezda != null && accommodationRepository.findByTripId(crvenaZvezda.getId()).isEmpty()) {
            addAccommodation(accommodationRepository, crvenaZvezda, "Hotel Slavija", "Svetog Save 1-9, Beograd", "48000", true);
            addAccommodation(accommodationRepository, crvenaZvezda, "Hotel Balkan", "Prizrenska 2, Beograd", "62000", false);
            addAccommodation(accommodationRepository, crvenaZvezda, "Garni hotel Trim", "Kneza Višeslava 72, Beograd", "39000", false);
        }
        if (crvenaZvezda != null && transportRepository.findByTripId(crvenaZvezda.getId()).isEmpty()) {
            addTransport(transportRepository, crvenaZvezda, "Jaćimović", "AUTOBUS", "55000", true);
            addTransport(transportRepository, crvenaZvezda, "Strela Obrenovac", "AUTOBUS", "51000", false);
            addTransport(transportRepository, crvenaZvezda, "Niš-ekspres", "AUTOBUS", "47000", false);
        }

        Trip budva = findTripByName(tripRepository, "CEV Kup – HOK Budva");
        if (budva != null && accommodationRepository.findByTripId(budva.getId()).isEmpty()) {
            addAccommodation(accommodationRepository, budva, "Hotel Mogren", "Mediteranska 21, Budva", "96000", true);
            addAccommodation(accommodationRepository, budva, "Hotel Slovenska plaža", "Trg Sunca 1, Budva", "88000", false);
            addAccommodation(accommodationRepository, budva, "Hotel Astoria", "Njegoševa 4, Budva", "120000", false);
        }
        if (budva != null && transportRepository.findByTripId(budva.getId()).isEmpty()) {
            addTransport(transportRepository, budva, "Jaćimović", "AUTOBUS", "142000", true);
            addTransport(transportRepository, budva, "Air Serbia", "AVION", "280000", false);
            addTransport(transportRepository, budva, "Montenegro Express", "AUTOBUS", "138000", false);
        }

        Trip zlatibor = findTripByName(tripRepository, "Pripremni kamp Zlatibor");
        if (zlatibor != null && accommodationRepository.findByTripId(zlatibor.getId()).isEmpty()) {
            addAccommodation(accommodationRepository, zlatibor, "Hotel Olimp", "Naselje Sloboda bb, Zlatibor", "320000", true);
            addAccommodation(accommodationRepository, zlatibor, "Hotel Mona", "Čajetina bb, Zlatibor", "410000", false);
            addAccommodation(accommodationRepository, zlatibor, "Apart hotel Zlatiborski konaci", "Miladina Pećinara 5, Zlatibor", "285000", false);
        }
        if (zlatibor != null && transportRepository.findByTripId(zlatibor.getId()).isEmpty()) {
            addTransport(transportRepository, zlatibor, "Jaćimović", "AUTOBUS", "78000", true);
            addTransport(transportRepository, zlatibor, "Saobraćaj Užice", "AUTOBUS", "71000", false);
            addTransport(transportRepository, zlatibor, "Prevoz Milovanović", "AUTOBUS", "64000", false);
        }

        Trip ribnica = findTripByName(tripRepository, "Superliga – OK Ribnica");
        if (ribnica != null && transportRepository.findByTripId(ribnica.getId()).isEmpty()) {
            addTransport(transportRepository, ribnica, "Jaćimović", "AUTOBUS", "46000", true);
            addTransport(transportRepository, ribnica, "Niš-ekspres", "AUTOBUS", "41000", false);
            addTransport(transportRepository, ribnica, "Bus Computers", "AUTOBUS", "42000", false);
        }

        Trip partizan = findTripByName(tripRepository, "Prijateljska – OK Partizan");
        if (partizan != null && transportRepository.findByTripId(partizan.getId()).isEmpty()) {
            addTransport(transportRepository, partizan, "Jaćimović", "AUTOBUS", "32000", true);
            addTransport(transportRepository, partizan, "Strela Obrenovac", "AUTOBUS", "35000", false);
        }
    }

    private Trip findTripByName(TripRepository tripRepository, String name) {
        Optional<Trip> trip = tripRepository.findByName(name);
        if (trip.isEmpty()) {
            return null;
        }
        return trip.get();
    }

    private void addAccommodation(AccommodationRepository accommodationRepository,
                                  Trip trip,
                                  String name,
                                  String address,
                                  String price,
                                  boolean selected) {
        var accommodation = new Accommodation();
        accommodation.setTrip(trip);
        accommodation.setName(name);
        accommodation.setAddress(address);
        accommodation.setPrice(new BigDecimal(price));
        accommodation.setSelected(selected);
        accommodationRepository.save(accommodation);
    }

    private void addTransport(TransportRepository transportRepository,
                              Trip trip,
                              String carrierName,
                              String transportType,
                              String price,
                              boolean selected) {
        var transport = new Transport();
        transport.setTrip(trip);
        transport.setCarrierName(carrierName);
        transport.setTransportType(transportType);
        transport.setPrice(new BigDecimal(price));
        transport.setSelected(selected);
        transportRepository.save(transport);
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
