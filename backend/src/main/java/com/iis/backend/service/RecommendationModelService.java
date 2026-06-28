package com.iis.backend.service;

import com.iis.backend.dto.RecommendationCriterionRequest;
import com.iis.backend.dto.RecommendationCriterionResponse;
import com.iis.backend.dto.RecommendationModelRequest;
import com.iis.backend.dto.RecommendationModelResponse;
import com.iis.backend.dto.RecommendationResultResponse;
import com.iis.backend.model.CriterionComparison;
import com.iis.backend.model.PerformanceValue;
import com.iis.backend.model.Player;
import com.iis.backend.model.RecommendationCriterion;
import com.iis.backend.model.RecommendationModel;
import com.iis.backend.model.RecommendationResult;
import com.iis.backend.model.RecommendationStatus;
import com.iis.backend.model.User;
import com.iis.backend.repository.MetricRepository;
import com.iis.backend.repository.PerformanceValueRepository;
import com.iis.backend.repository.PlayerRepository;
import com.iis.backend.repository.RecommendationModelRepository;
import com.iis.backend.repository.RecommendationResultRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecommendationModelService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final RecommendationModelRepository modelRepository;
    private final RecommendationResultRepository resultRepository;
    private final MetricRepository metricRepository;
    private final PlayerRepository playerRepository;
    private final PerformanceValueRepository performanceRepository;

    public RecommendationModelService(
            RecommendationModelRepository modelRepository,
            RecommendationResultRepository resultRepository,
            MetricRepository metricRepository,
            PlayerRepository playerRepository,
            PerformanceValueRepository performanceRepository) {
        this.modelRepository = modelRepository;
        this.resultRepository = resultRepository;
        this.metricRepository = metricRepository;
        this.playerRepository = playerRepository;
        this.performanceRepository = performanceRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationModelResponse> findAll() {
        return modelRepository.findAll().stream()
                .sorted(Comparator.comparing(RecommendationModel::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RecommendationModelResponse create(RecommendationModelRequest request, User director) {
        validateWeights(request.criteria());
        var model = new RecommendationModel(
                request.name().trim(),
                normalizeOptional(request.position()),
                request.minimumHeight(),
                request.minimumScore(),
                director,
                LocalDateTime.now());
        model.replaceCriteria(buildCriteria(model, request.criteria()));
        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public RecommendationModelResponse update(Long id, RecommendationModelRequest request) {
        validateWeights(request.criteria());
        var model = findModel(id);
        model.setName(request.name().trim());
        model.setPosition(normalizeOptional(request.position()));
        model.setMinimumHeight(request.minimumHeight());
        model.setMinimumScore(request.minimumScore());
        model.replaceCriteria(buildCriteria(model, request.criteria()));
        resultRepository.deleteByModelId(id);
        return toResponse(modelRepository.save(model));
    }

    @Transactional
    public void delete(Long id) {
        var model = findModel(id);
        resultRepository.deleteByModelId(id);
        modelRepository.delete(model);
    }

    @Transactional
    public List<RecommendationResultResponse> calculate(Long modelId) {
        var model = findModel(modelId);
        var performances = averagePerformances();
        resultRepository.deleteByModelId(modelId);

        var results = playerRepository.findAll().stream()
                .filter(player -> matchesBasicParameters(player, model))
                .map(player -> calculateForPlayer(model, player, performances.getOrDefault(player.getId(), Map.of())))
                .map(resultRepository::save)
                .sorted(Comparator.comparing(RecommendationResult::getScore).reversed())
                .map(this::toResultResponse)
                .toList();

        if (results.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nema igraca koji odgovaraju poziciji i osnovnim parametrima modela");
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<RecommendationResultResponse> findResults(Long modelId) {
        if (!modelRepository.existsById(modelId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Model preporuke nije pronadjen");
        }
        return resultRepository.findByModelIdOrderByScoreDesc(modelId).stream()
                .map(this::toResultResponse)
                .toList();
    }

    private RecommendationResult calculateForPlayer(RecommendationModel model, Player player, Map<Long, BigDecimal> values) {
        BigDecimal weightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        boolean requiredFailed = false;
        var explanation = new StringBuilder();

        for (var criterion : model.getCriteria()) {
            var actual = values.get(criterion.getMetric().getId());
            var criterionScore = scoreCriterion(actual, criterion);
            var passed = passes(actual, criterion);
            if (criterion.isRequired() && !passed) {
                requiredFailed = true;
            }
            weightedScore = weightedScore.add(criterionScore.multiply(criterion.getWeight()));
            totalWeight = totalWeight.add(criterion.getWeight());
            appendExplanation(explanation, criterion, actual, passed);
        }

        var score = totalWeight.signum() == 0
                ? BigDecimal.ZERO
                : weightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
        var status = resolveStatus(score, model.getMinimumScore(), requiredFailed);
        return new RecommendationResult(model, player, score, status, explanation.toString(), LocalDateTime.now());
    }

    private BigDecimal scoreCriterion(BigDecimal actual, RecommendationCriterion criterion) {
        if (actual == null) {
            return BigDecimal.ZERO;
        }
        var target = criterion.getThresholdValue();
        if (criterion.getComparison() == CriterionComparison.MINIMUM) {
            if (actual.compareTo(target) >= 0) {
                return HUNDRED;
            }
            return actual.multiply(HUNDRED).divide(target, 2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        }
        if (actual.compareTo(target) <= 0) {
            return HUNDRED;
        }
        return target.multiply(HUNDRED).divide(actual, 2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
    }

    private boolean passes(BigDecimal actual, RecommendationCriterion criterion) {
        if (actual == null) {
            return false;
        }
        return criterion.getComparison() == CriterionComparison.MINIMUM
                ? actual.compareTo(criterion.getThresholdValue()) >= 0
                : actual.compareTo(criterion.getThresholdValue()) <= 0;
    }

    private RecommendationStatus resolveStatus(BigDecimal score, BigDecimal minimumScore, boolean requiredFailed) {
        if (requiredFailed || score.compareTo(minimumScore) < 0) {
            return score.compareTo(BigDecimal.valueOf(50)) >= 0
                    ? RecommendationStatus.NASTAVITI_PRACENJE
                    : RecommendationStatus.NE_ISPUNJAVA;
        }
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return RecommendationStatus.PREPORUCEN;
        }
        if (score.compareTo(BigDecimal.valueOf(65)) >= 0) {
            return RecommendationStatus.UZI_IZBOR;
        }
        return RecommendationStatus.NASTAVITI_PRACENJE;
    }

    private Map<Long, Map<Long, BigDecimal>> averagePerformances() {
        var sums = new HashMap<Long, Map<Long, BigDecimal>>();
        var counts = new HashMap<Long, Map<Long, Integer>>();
        for (PerformanceValue performance : performanceRepository.findAll()) {
            var playerId = performance.getPlayer().getId();
            var metricId = performance.getMetric().getId();
            sums.computeIfAbsent(playerId, key -> new HashMap<>())
                    .merge(metricId, performance.getValue(), BigDecimal::add);
            counts.computeIfAbsent(playerId, key -> new HashMap<>())
                    .merge(metricId, 1, Integer::sum);
        }
        sums.forEach((playerId, metricSums) -> metricSums.replaceAll((metricId, sum) ->
                sum.divide(BigDecimal.valueOf(counts.get(playerId).get(metricId)), 2, RoundingMode.HALF_UP)));
        return sums;
    }

    private boolean matchesBasicParameters(Player player, RecommendationModel model) {
        var matchesPosition = model.getPosition() == null
                || player.getPosition() != null && model.getPosition().equalsIgnoreCase(player.getPosition().getName());
        var matchesHeight = model.getMinimumHeight() == null
                || player.getHeight() != null && player.getHeight() >= model.getMinimumHeight();
        return matchesPosition && matchesHeight;
    }

    private List<RecommendationCriterion> buildCriteria(RecommendationModel model, List<RecommendationCriterionRequest> requests) {
        var criteria = new ArrayList<RecommendationCriterion>();
        for (var request : requests) {
            var metric = metricRepository.findById(request.metricId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Metrika nije pronadjena"));
            criteria.add(new RecommendationCriterion(
                    model, metric, request.comparison(), request.thresholdValue(), request.weight(), request.required()));
        }
        return criteria;
    }

    private void validateWeights(List<RecommendationCriterionRequest> criteria) {
        var total = criteria.stream().map(RecommendationCriterionRequest::weight).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(HUNDRED) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zbir tezina kriterijuma mora biti 100");
        }
    }

    private RecommendationModel findModel(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Model preporuke nije pronadjen"));
    }

    private void appendExplanation(StringBuilder explanation, RecommendationCriterion criterion, BigDecimal actual, boolean passed) {
        if (!explanation.isEmpty()) {
            explanation.append(" | ");
        }
        explanation.append(criterion.getMetric().getName())
                .append(": ")
                .append(actual == null ? "nema podatka" : actual.stripTrailingZeros().toPlainString())
                .append(passed ? " (ispunjeno)" : " (nije ispunjeno)");
    }

    private RecommendationModelResponse toResponse(RecommendationModel model) {
        var criteria = model.getCriteria().stream()
                .map(criterion -> new RecommendationCriterionResponse(
                        criterion.getId(), criterion.getMetric().getId(), criterion.getMetric().getName(),
                        criterion.getMetric().getUnitOfMeasure(), criterion.getComparison(),
                        criterion.getThresholdValue(), criterion.getWeight(), criterion.isRequired()))
                .toList();
        return new RecommendationModelResponse(
                model.getId(), model.getName(), model.getPosition(), model.getMinimumHeight(), model.getMinimumScore(),
                model.getCreatedBy().getFirstName() + " " + model.getCreatedBy().getLastName(), model.getCreatedAt(), criteria);
    }

    private RecommendationResultResponse toResultResponse(RecommendationResult result) {
        var player = result.getPlayer();
        return new RecommendationResultResponse(
                result.getId(), result.getModel().getId(), player.getId(),
                player.getFirstName() + " " + player.getLastName(),
                player.getPosition() == null ? null : player.getPosition().getName(),
                player.getClub() == null ? null : player.getClub().getName(), player.getHeight(),
                result.getScore(), result.getStatus(), result.getExplanation(), result.getCalculatedAt());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
