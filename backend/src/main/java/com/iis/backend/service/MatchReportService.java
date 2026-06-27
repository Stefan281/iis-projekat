package com.iis.backend.service;

import com.iis.backend.dto.ActivityRecommendationResponse;
import com.iis.backend.dto.AnalysisPlayerResponse;
import com.iis.backend.dto.MatchEventResponse;
import com.iis.backend.dto.MatchStatisticsResponse;
import com.iis.backend.dto.PlayerAnalysisResponse;
import com.iis.backend.dto.PlayerStatisticResponse;
import com.iis.backend.dto.TeamAnalysisResponse;
import com.iis.backend.dto.TeamStatisticResponse;
import com.iis.backend.repository.MatchEventRepository;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

    private final MatchService matchService;
    private final MatchStatisticsService matchStatisticsService;
    private final MatchEventRepository matchEventRepository;

    public MatchReportService(
            MatchService matchService,
            MatchStatisticsService matchStatisticsService,
            MatchEventRepository matchEventRepository) {
        this.matchService = matchService;
        this.matchStatisticsService = matchStatisticsService;
        this.matchEventRepository = matchEventRepository;
    }

    public byte[] generateCurrentMatchReport() {
        try {
            var match = matchService.getCurrentMatch();
            var statistics = matchStatisticsService.getCurrentMatchStatistics();

            try (var pdf = new PdfReportWriter()) {
                pdf.title("Izveštaj utakmice");
                pdf.text(match.homeTeam().name() + " vs " + match.awayTeam().name());
                pdf.text("Datum: " + statistics.matchDate());
                pdf.text("Status: " + statusLabel(statistics.status()));
                pdf.text("Rezultat: " + value(statistics.result()));
                pdf.blank();

                addTeamStatistics(pdf, statistics);
                addTeamAnalyses(pdf, statistics);
                addPlayerStatistics(pdf, "Statistika igrača - " + statistics.homeTeam().teamName(), statistics.homePlayers());
                addPlayerStatistics(pdf, "Statistika igrača - " + statistics.awayTeam().teamName(), statistics.awayPlayers());
                addPlayerAnalyses(pdf, "Analiza igrača - " + statistics.homeTeam().teamName(), statistics.homePlayerAnalyses());
                addPlayerAnalyses(pdf, "Analiza igrača - " + statistics.awayTeam().teamName(), statistics.awayPlayerAnalyses());
                addRecommendations(pdf, statistics.recommendations());
                addEvents(pdf, allEvents(match.id()));

                return pdf.build();
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nije moguće generisati izveštaj.", exception);
        }
    }

    private void addTeamStatistics(PdfReportWriter pdf, MatchStatisticsResponse statistics) throws IOException {
        pdf.heading("Statistika timova");
        pdf.table(
                List.of("Tim", "Setovi", "Poeni", "Greške", "Servisi", "Blokovi", "Izmene"),
                List.of(teamStatisticRow(statistics.homeTeam()), teamStatisticRow(statistics.awayTeam())));
        pdf.blank();
    }

    private List<String> teamStatisticRow(TeamStatisticResponse statistic) {
        return List.of(
                statistic.teamName(),
                value(statistic.setsWon()),
                value(statistic.points()),
                value(statistic.errors()),
                value(statistic.serves()),
                value(statistic.blocks()),
                value(statistic.substitutions()));
    }

    private void addTeamAnalyses(PdfReportWriter pdf, MatchStatisticsResponse statistics) throws IOException {
        pdf.heading("Analiza timova");
        addTeamAnalysis(pdf, statistics.homeAnalysis());
        addTeamAnalysis(pdf, statistics.awayAnalysis());
        pdf.blank();
    }

    private void addTeamAnalysis(PdfReportWriter pdf, TeamAnalysisResponse analysis) throws IOException {
        if (analysis == null) {
            pdf.text("Nema podataka za analizu tima.");
            return;
        }

        pdf.subheading(analysis.teamName());
        pdf.table(
                List.of("Metrika", "Vrednost"),
                List.of(
                        List.of("Efikasnost tima", score(analysis.teamEfficiency())),
                        List.of("Indeks napada", score(analysis.attackIndex())),
                        List.of("Indeks servisa", score(analysis.serveIndex())),
                        List.of("Indeks bloka", score(analysis.blockIndex())),
                        List.of("Disciplina", score(analysis.disciplineIndex()))));
        pdf.table(
                List.of("Kategorija", "Igrač"),
                List.of(
                        List.of("Najefikasniji igrač", analysisPlayer(analysis.mostEfficientPlayer())),
                        List.of("Najmanje efikasan igrač", analysisPlayer(analysis.leastEfficientPlayer())),
                        List.of("Najviše poena", analysisPlayer(analysis.topPointsPlayer())),
                        List.of("Najviše grešaka", analysisPlayer(analysis.topErrorsPlayer())),
                        List.of("Najviše blokova", analysisPlayer(analysis.topBlocksPlayer())),
                        List.of("Najviše servisa", analysisPlayer(analysis.topServesPlayer())),
                        List.of("Najviše asistencija", analysisPlayer(analysis.topAssistsPlayer()))));
    }

    private void addPlayerStatistics(PdfReportWriter pdf, String title, List<PlayerStatisticResponse> players) throws IOException {
        pdf.heading(title);
        if (players.isEmpty()) {
            pdf.text("Nema unetih statistika igrača.");
            pdf.blank();
            return;
        }

        pdf.table(
                List.of("Br", "Igrač", "Status", "P", "G", "S", "B", "A"),
                players.stream()
                        .map(player -> List.of(
                                value(player.jerseyNumber()),
                                player.playerName(),
                                playerStatusLabel(player.playerStatus()),
                                value(player.points()),
                                value(player.errors()),
                                value(player.serves()),
                                value(player.blocks()),
                                value(player.assists())))
                        .toList());
        pdf.blank();
    }

    private void addPlayerAnalyses(PdfReportWriter pdf, String title, List<PlayerAnalysisResponse> analyses) throws IOException {
        pdf.heading(title);
        if (analyses.isEmpty()) {
            pdf.text("Nema podataka za analizu igrača.");
            pdf.blank();
            return;
        }

        pdf.table(
                List.of("Br", "Igrač", "Efikasnost", "Servis", "Ukupno"),
                analyses.stream()
                        .map(analysis -> List.of(
                                value(analysis.jerseyNumber()),
                                analysis.playerName(),
                                score(analysis.efficiency()),
                                score(analysis.serveContribution()),
                                score(analysis.overallRating())))
                        .toList());
        pdf.blank();
    }

    private void addRecommendations(PdfReportWriter pdf, List<ActivityRecommendationResponse> recommendations) throws IOException {
        pdf.heading("Preporuke aktivnosti");
        if (recommendations.isEmpty()) {
            pdf.text("Nema generisanih preporuka.");
            pdf.blank();
            return;
        }

        for (ActivityRecommendationResponse recommendation : recommendations) {
            pdf.subheading(priorityLabel(recommendation.priority()) + " - " + recommendation.title());
            pdf.text("Tim: " + recommendation.teamName());
            if (recommendation.playerName() != null) {
                pdf.text("Igrač: #" + recommendation.jerseyNumber() + " " + recommendation.playerName());
            }
            pdf.text(recommendation.description());
        }
        pdf.blank();
    }

    private void addEvents(PdfReportWriter pdf, List<MatchEventResponse> events) throws IOException {
        pdf.heading("Evidentirani događaji");
        if (events.isEmpty()) {
            pdf.text("Nema evidentiranih događaja.");
            return;
        }

        pdf.table(
                List.of("Vreme", "Tim", "Igrač", "Događaj"),
                events.stream()
                        .map(event -> List.of(
                                event.eventTime().format(DATE_TIME_FORMATTER),
                                event.teamName(),
                                eventPlayer(event),
                                eventLabel(event.eventType().name())))
                        .toList());
    }

    private List<MatchEventResponse> allEvents(Long matchId) {
        return matchEventRepository.findByMatchIdOrderByEventTimeAsc(matchId).stream()
                .map(MatchEventResponse::fromEntity)
                .toList();
    }

    private String analysisPlayer(AnalysisPlayerResponse player) {
        if (player == null) {
            return "Nema podataka";
        }

        return "#" + player.jerseyNumber() + " " + player.playerName()
                + " (P: " + player.points()
                + ", G: " + player.errors()
                + ", B: " + player.blocks()
                + ", E: " + player.efficiency() + ")";
    }

    private String eventPlayer(MatchEventResponse event) {
        var player = "#" + event.jerseyNumber() + " " + event.playerName();
        if (event.secondaryPlayerName() == null) {
            return player;
        }

        return player + " -> #" + event.secondaryJerseyNumber() + " " + event.secondaryPlayerName();
    }

    private String eventLabel(String eventType) {
        return switch (eventType) {
            case "POINT" -> "Poen";
            case "ERROR" -> "Greška";
            case "SERVE" -> "Servis";
            case "ASSIST" -> "Asistencija";
            case "BLOCK" -> "Blok";
            case "SUBSTITUTION" -> "Izmena";
            default -> eventType;
        };
    }

    private String playerStatusLabel(String status) {
        return switch (status) {
            case "IN_GAME" -> "U igri";
            case "BENCH" -> "Klupa";
            case "INACTIVE" -> "Neaktivan";
            default -> status;
        };
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "SCHEDULED" -> "Zakazana";
            case "IN_PROGRESS" -> "U toku";
            case "FINISHED" -> "Završena";
            default -> status;
        };
    }

    private String priorityLabel(String priority) {
        return switch (priority) {
            case "HIGH" -> "Visok prioritet";
            case "MEDIUM" -> "Srednji prioritet";
            case "LOW" -> "Nizak prioritet";
            default -> priority;
        };
    }

    private String score(Integer value) {
        return value(value) + "/10";
    }

    private String value(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private static final class PdfReportWriter implements AutoCloseable {
        private static final float LEFT = 42;
        private static final float TOP = 800;
        private static final float BOTTOM = 46;
        private static final float LINE_GAP = 5;

        private final PDDocument document;
        private final PDFont regularFont;
        private final PDFont boldFont;
        private PDPageContentStream content;
        private float y;

        private PdfReportWriter() throws IOException {
            document = new PDDocument();
            regularFont = PDType0Font.load(document, fontFile("arial.ttf"));
            boldFont = PDType0Font.load(document, fontFile("arialbd.ttf"));
            newPage();
        }

        private static File fontFile(String fileName) {
            return new File("C:\\Windows\\Fonts\\" + fileName);
        }

        private void title(String text) throws IOException {
            line(text, 20, boldFont);
            blank();
        }

        private void heading(String text) throws IOException {
            blank();
            line(text, 15, boldFont);
        }

        private void subheading(String text) throws IOException {
            line(text, 12, boldFont);
        }

        private void text(String text) throws IOException {
            for (String line : wrap(text, 96)) {
                line(line, 10, regularFont);
            }
        }

        private void blank() {
            y -= 10;
        }

        private void table(List<String> headers, List<List<String>> rows) throws IOException {
            line(String.join(" | ", headers), 9, boldFont);
            line("-".repeat(Math.min(110, String.join(" | ", headers).length() + 18)), 9, regularFont);
            for (List<String> row : rows) {
                for (String line : wrap(String.join(" | ", row), 105)) {
                    line(line, 9, regularFont);
                }
            }
            blank();
        }

        private void line(String text, int fontSize, PDFont font) throws IOException {
            if (y < BOTTOM + 20) {
                newPage();
            }

            content.beginText();
            content.setFont(font, fontSize);
            content.newLineAtOffset(LEFT, y);
            content.showText(text == null ? "" : text);
            content.endText();
            y -= fontSize + LINE_GAP;
        }

        private List<String> wrap(String text, int maxLength) {
            var safeText = text == null ? "" : text;
            if (safeText.length() <= maxLength) {
                return List.of(safeText);
            }

            var lines = new java.util.ArrayList<String>();
            var current = new StringBuilder();
            for (String word : safeText.split(" ")) {
                if (current.length() + word.length() + 1 > maxLength) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
                if (!current.isEmpty()) {
                    current.append(' ');
                }
                current.append(word);
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
            return lines;
        }

        private void newPage() throws IOException {
            if (content != null) {
                content.close();
            }

            var page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = TOP;
        }

        private byte[] build() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }

            var output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        }

        @Override
        public void close() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
            document.close();
        }
    }
}
