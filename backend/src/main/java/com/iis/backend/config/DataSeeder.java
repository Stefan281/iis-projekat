package com.iis.backend.config;

import com.iis.backend.model.Match;
import com.iis.backend.model.MatchStatus;
import com.iis.backend.model.OpponentPlayer;
import com.iis.backend.model.OpponentTeam;
import com.iis.backend.model.PlayerStatus;
import com.iis.backend.model.Role;
import com.iis.backend.model.User;
import com.iis.backend.repository.MatchRepository;
import com.iis.backend.repository.OpponentTeamRepository;
import com.iis.backend.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(
            UserRepository userRepository,
            OpponentTeamRepository opponentTeamRepository,
            MatchRepository matchRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {
        return args -> {
            jdbcTemplate.execute("ALTER TABLE IF EXISTS match_events DROP COLUMN IF EXISTS description");
            jdbcTemplate.execute("""
                    DO $$
                    DECLARE
                        constraint_record record;
                    BEGIN
                        FOR constraint_record IN
                            SELECT conname
                            FROM pg_constraint
                            WHERE conrelid = 'match_events'::regclass
                              AND pg_get_constraintdef(oid) LIKE '%event_type%'
                        LOOP
                            EXECUTE format('ALTER TABLE match_events DROP CONSTRAINT IF EXISTS %I', constraint_record.conname);
                        END LOOP;
                    END $$;
                    """);

            createUserIfMissing(userRepository, passwordEncoder, "admin", "admin123", "Admin", "Korisnik", Role.ADMIN);
            createUserIfMissing(userRepository, passwordEncoder, "statisticar", "statisticar123", "Marko", "Statisticar", Role.STATISTICAR);
            createUserIfMissing(userRepository, passwordEncoder, "stab", "stab123", "Ana", "Strucni stab", Role.STRUCNI_STAB);

            /*if (matchRepository.findFirstByStatusOrderByMatchDateDesc(MatchStatus.IN_PROGRESS).isEmpty()) {
                var homeTeam = createTeamIfMissing(opponentTeamRepository, "Nas tim", "Novi Sad", "Milan Trener");
                addPlayerIfMissing(homeTeam, "Ana Petrovic", 7, "Primac", 182, 24, PlayerStatus.IN_GAME);
                addPlayerIfMissing(homeTeam, "Pera Peric", 12, "Korektor", 190, 26, PlayerStatus.IN_GAME);
                addPlayerIfMissing(homeTeam, "Jovana Jovanovic", 5, "Tehnicar", 176, 22, PlayerStatus.IN_GAME);
                addPlayerIfMissing(homeTeam, "Marko Maric", 4, "Srednji bloker", 198, 25, PlayerStatus.IN_GAME);
                addPlayerIfMissing(homeTeam, "Milos Mikic", 1, "Libero", 181, 21, PlayerStatus.IN_GAME);
                addPlayerIfMissing(homeTeam, "Sara Stojanovic", 13, "Primac", 184, 23, PlayerStatus.IN_GAME);
                addPlayerIfMissing(homeTeam, "Nikola Nikolic", 8, "Primac", 186, 20, PlayerStatus.BENCH);
                addPlayerIfMissing(homeTeam, "Luka Lukic", 10, "Korektor", 193, 24, PlayerStatus.BENCH);
                opponentTeamRepository.save(homeTeam);

                var awayTeam = createTeamIfMissing(opponentTeamRepository, "Partizan", "Beograd", "Petar Trener");
                addPlayerIfMissing(awayTeam, "Ana Petrovic", 7, "Primac", 181, 24, PlayerStatus.IN_GAME);
                addPlayerIfMissing(awayTeam, "Pera Peric", 12, "Korektor", 191, 26, PlayerStatus.IN_GAME);
                addPlayerIfMissing(awayTeam, "Jovana Jovanovic", 5, "Tehnicar", 177, 22, PlayerStatus.IN_GAME);
                addPlayerIfMissing(awayTeam, "Marko Maric", 4, "Srednji bloker", 199, 25, PlayerStatus.IN_GAME);
                addPlayerIfMissing(awayTeam, "Milos Mikic", 1, "Libero", 180, 21, PlayerStatus.IN_GAME);
                addPlayerIfMissing(awayTeam, "Sara Stojanovic", 13, "Primac", 185, 23, PlayerStatus.IN_GAME);
                addPlayerIfMissing(awayTeam, "Nikola Nikolic", 8, "Primac", 187, 20, PlayerStatus.BENCH);
                addPlayerIfMissing(awayTeam, "Luka Lukic", 10, "Korektor", 194, 24, PlayerStatus.BENCH);
                opponentTeamRepository.save(awayTeam);

                var match = new Match();
                match.setHomeTeam(homeTeam);
                match.setAwayTeam(awayTeam);
                match.setMatchDate(LocalDate.now());
                match.setResult("0 : 0");
                match.setStatus(MatchStatus.IN_PROGRESS);
                matchRepository.save(match);
            }

             */
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

    /*
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
            Integer age,
            PlayerStatus playerStatus) {
        var existingPlayer = team.getPlayers().stream()
                .filter(player -> player.getJerseyNumber().equals(jerseyNumber))
                .findFirst();

        if (existingPlayer.isPresent()) {
            existingPlayer.get().setPlayerStatus(playerStatus);
            return;
        }

        var player = new OpponentPlayer();
        player.setFullName(fullName);
        player.setJerseyNumber(jerseyNumber);
        player.setPosition(position);
        player.setHeight(height);
        player.setAge(age);
        player.setPlayerStatus(playerStatus);
        team.addPlayer(player);
    }
     */
}
