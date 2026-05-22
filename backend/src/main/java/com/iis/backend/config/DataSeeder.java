package com.iis.backend.config;

import com.iis.backend.model.Match;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.model.OpponentPlayer;
import com.iis.backend.model.OpponentTeam;
import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.OpponentTeamRepository;
import com.iis.backend.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(
            UserRepository userRepository,
            OpponentTeamRepository opponentTeamRepository,
            MatchRepository matchRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(userRepository, passwordEncoder, "admin", "admin123", "Admin", "Korisnik", Role.ADMIN);
            createUserIfMissing(userRepository, passwordEncoder, "statisticar", "statisticar123", "Marko", "Statisticar", Role.STATISTICAR);
            createUserIfMissing(userRepository, passwordEncoder, "stab", "stab123", "Ana", "Strucni stab", Role.STRUCNI_STAB);

            var homeTeam = createTeamIfMissing(opponentTeamRepository, "Nas tim", "Novi Sad", "Milan Trener");
            addPlayerIfMissing(homeTeam, "Ana Petrovic", 7, "Primac", 182, 24);
            addPlayerIfMissing(homeTeam, "Pera Peric", 12, "Korektor", 190, 26);
            addPlayerIfMissing(homeTeam, "Jovana Jovanovic", 5, "Tehnicar", 176, 22);
            addPlayerIfMissing(homeTeam, "Marko Maric", 4, "Srednji bloker", 198, 25);
            addPlayerIfMissing(homeTeam, "Milos Mikic", 1, "Libero", 181, 21);
            addPlayerIfMissing(homeTeam, "Sara Stojanovic", 13, "Primac", 184, 23);
            opponentTeamRepository.save(homeTeam);

            var awayTeam = createTeamIfMissing(opponentTeamRepository, "Partizan", "Beograd", "Petar Trener");
            addPlayerIfMissing(awayTeam, "Ana Petrovic", 7, "Primac", 181, 24);
            addPlayerIfMissing(awayTeam, "Pera Peric", 12, "Korektor", 191, 26);
            addPlayerIfMissing(awayTeam, "Jovana Jovanovic", 5, "Tehnicar", 177, 22);
            addPlayerIfMissing(awayTeam, "Marko Maric", 4, "Srednji bloker", 199, 25);
            addPlayerIfMissing(awayTeam, "Milos Mikic", 1, "Libero", 180, 21);
            addPlayerIfMissing(awayTeam, "Sara Stojanovic", 13, "Primac", 185, 23);
            opponentTeamRepository.save(awayTeam);

            if (matchRepository.findFirstByStatusOrderByMatchDateDesc(MatchStatus.IN_PROGRESS).isEmpty()) {
                var match = new Match();
                match.setHomeTeam(homeTeam);
                match.setAwayTeam(awayTeam);
                match.setMatchDate(LocalDate.now());
                match.setResult("0 : 0");
                match.setStatus(MatchStatus.IN_PROGRESS);
                matchRepository.save(match);
            }
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

    private OpponentTeam createTeamIfMissing(
            OpponentTeamRepository opponentTeamRepository,
            String name,
            String city,
            String coach) {
        return opponentTeamRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    var team = new OpponentTeam();
                    team.setName(name);
                    team.setWins(0);
                    team.setLosses(0);
                    team.setCity(city);
                    team.setCoach(coach);
                    team.setPlayStyle("");
                    team.setNote(null);
                    return opponentTeamRepository.save(team);
                });
    }

    private void addPlayerIfMissing(
            OpponentTeam team,
            String fullName,
            Integer jerseyNumber,
            String position,
            Integer height,
            Integer age) {
        var exists = team.getPlayers().stream()
                .anyMatch(player -> player.getJerseyNumber().equals(jerseyNumber));

        if (exists) {
            return;
        }

        var player = new OpponentPlayer();
        player.setFullName(fullName);
        player.setJerseyNumber(jerseyNumber);
        player.setPosition(position);
        player.setHeight(height);
        player.setAge(age);
        team.addPlayer(player);
    }
}
