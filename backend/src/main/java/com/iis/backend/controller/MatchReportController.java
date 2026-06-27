package com.iis.backend.controller;

import com.iis.backend.service.MatchReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchReportController {

    private final MatchReportService matchReportService;

    public MatchReportController(MatchReportService matchReportService) {
        this.matchReportService = matchReportService;
    }

    @GetMapping("/current/report")
    public ResponseEntity<byte[]> generateCurrentMatchReport() {
        var report = matchReportService.generateCurrentMatchReport();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("izvestaj-aktuelne-utakmice.pdf").build().toString())
                .body(report);
    }
}
