package com.iis.backend.controller;

import com.iis.backend.dto.CostAnalysisResponseDto;
import com.iis.backend.service.CostAnalysisService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cost-analysis")
public class CostAnalysisController {

    private static final String REQUIRED_ROLE = "ROLE_GENERALNI_DIREKTOR";

    private final CostAnalysisService costAnalysisService;

    public CostAnalysisController(CostAnalysisService costAnalysisService) {
        this.costAnalysisService = costAnalysisService;
    }

    @GetMapping
    public ResponseEntity<CostAnalysisResponseDto> getCostAnalysis(
            @RequestParam(value = "year") int year,
            @RequestParam(value = "month", required = false, defaultValue = "0") int month,
            Authentication authentication) {
        requireDirector(authentication);
        CostAnalysisResponseDto response = costAnalysisService.getCostAnalysis(year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> getReport(
            @RequestParam(value = "year") int year,
            @RequestParam(value = "month", required = false, defaultValue = "0") int month,
            Authentication authentication) {
        requireDirector(authentication);
        byte[] pdf = costAnalysisService.generateReport(year, month);

        String filename = costAnalysisService.buildReportFileName(year, month);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        return new ResponseEntity<byte[]>(pdf, headers, HttpStatus.OK);
    }

    private void requireDirector(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pristup nije dozvoljen");
        }
        boolean allowed = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (REQUIRED_ROLE.equals(authority.getAuthority())) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pristup nije dozvoljen");
        }
    }
}
