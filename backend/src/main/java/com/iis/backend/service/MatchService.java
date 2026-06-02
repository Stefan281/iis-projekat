package com.iis.backend.service;

import com.iis.backend.dto.MatchRequest;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Match;
import com.iis.backend.repository.MatchRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MatchService {
    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public List<Match> findAll() { return matchRepository.findAll(); }

    public Match findById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match was not found"));
    }

    public Match create(MatchRequest request) {
        return matchRepository.save(mapToEntity(new Match(), request));
    }

    public Match update(Long id, MatchRequest request) {
        return matchRepository.save(mapToEntity(findById(id), request));
    }

    public void delete(Long id) {
        matchRepository.delete(findById(id));
    }

    private Match mapToEntity(Match match, MatchRequest request) {
        match.setDate(request.date());
        match.setTime(request.time());
        match.setHomeTeam(request.homeTeam());
        match.setAwayTeam(request.awayTeam());
        match.setLocation(request.location());
        match.setStatus(request.status());
        match.setBasePrice(request.basePrice());
        match.setAttractiveness(request.attractiveness());
        match.setExpectedAttendance(request.expectedAttendance());
        return match;
    }
}
