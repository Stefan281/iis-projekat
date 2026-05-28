package com.iis.backend.service;

import com.iis.backend.dto.CreatePlayerRequest;
import com.iis.backend.dto.PlayerResponse;
import com.iis.backend.model.Club;
import com.iis.backend.model.Player;
import com.iis.backend.model.Position;
import com.iis.backend.repository.ClubRepository;
import com.iis.backend.repository.PlayerRepository;
import com.iis.backend.repository.PositionRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final PositionRepository positionRepository;

    public PlayerService(
            PlayerRepository playerRepository,
            ClubRepository clubRepository,
            PositionRepository positionRepository) {
        this.playerRepository = playerRepository;
        this.clubRepository = clubRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public PlayerResponse create(CreatePlayerRequest request) {
        var club = clubRepository.findByNameIgnoreCase(request.club().trim())
                .orElseGet(() -> clubRepository.save(new Club(request.club().trim(), null, null)));
        var position = positionRepository.findByNameIgnoreCase(request.position().trim())
                .orElseGet(() -> positionRepository.save(new Position(request.position().trim())));

        var player = new Player(
                request.firstName().trim(),
                request.lastName().trim(),
                LocalDate.of(request.birthYear(), 1, 1),
                request.height(),
                normalizeOptional(request.note()),
                club,
                position);

        return toResponse(playerRepository.save(player));
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> findAll() {
        return playerRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Player::getId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> findRecent() {
        return playerRepository.findTop5ByOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return playerRepository.count();
    }

    @Transactional(readOnly = true)
    public PlayerResponse findById(Long id) {
        return playerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));
    }

    @Transactional
    public PlayerResponse update(Long id, CreatePlayerRequest request) {
        var player = playerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Igrac nije pronadjen"));
        var club = clubRepository.findByNameIgnoreCase(request.club().trim())
                .orElseGet(() -> clubRepository.save(new Club(request.club().trim(), null, null)));
        var position = positionRepository.findByNameIgnoreCase(request.position().trim())
                .orElseGet(() -> positionRepository.save(new Position(request.position().trim())));

        player.setFirstName(request.firstName().trim());
        player.setLastName(request.lastName().trim());
        player.setBirthDate(LocalDate.of(request.birthYear(), 1, 1));
        player.setHeight(request.height());
        player.setBasicInfo(normalizeOptional(request.note()));
        player.setClub(club);
        player.setPosition(position);

        return toResponse(playerRepository.save(player));
    }

    public PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getBirthDate() == null ? null : player.getBirthDate().getYear(),
                player.getHeight(),
                player.getPosition() == null ? null : player.getPosition().getName(),
                player.getClub() == null ? null : player.getClub().getName(),
                player.getBasicInfo());
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
