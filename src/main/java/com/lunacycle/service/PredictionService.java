package com.lunacycle.service;

import com.lunacycle.dto.PredictionDto;
import com.lunacycle.model.Cycle;
import com.lunacycle.repository.CycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final CycleRepository cycleRepository;
    private final RestClient restClient;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Value("${ai.service.secret}")
    private String aiServiceSecret; // <-- Make sure this is injected!

    // Threshold: less than 3 cycles → use math. 3 or more → call AI
    private static final int AI_THRESHOLD = 3;

    public PredictionDto.PredictionResponse predict(UUID userId) {
        List<Cycle> cycles = cycleRepository
                .findByUserIdOrderByStartDateDesc(userId);

        if (cycles.isEmpty()) {
            return noDataResponse();
        }

        List<Integer> lengths = cycles.stream()
                .filter(c -> c.getCycleLengthDays() != null)
                .map(Cycle::getCycleLengthDays)
                .collect(Collectors.toList());

        LocalDate lastStart = cycles.get(0).getStartDate();

        // Level 1 — Math (new users, fewer than 3 cycles)
        if (lengths.size() < AI_THRESHOLD) {
            return mathPrediction(lastStart, lengths);
        }

        // Level 2 — AI (long-term users, 3+ cycles)
        try {
            return aiPrediction(lastStart, lengths);
        } catch (Exception e) {
            log.warn("AI service unavailable, falling back to math: {}",
                    e.getMessage());
            return mathPrediction(lastStart, lengths);
        }
    }

    // ── Level 1: Simple moving average ──────────────────────────────────────
    private PredictionDto.PredictionResponse mathPrediction(
            LocalDate lastStart, List<Integer> lengths) {

        int avgLength = lengths.isEmpty() ? 28
                : (int) lengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(28);

        LocalDate predictedStart = lastStart.plusDays(avgLength);

        // ±2 day window for math predictions
        LocalDate windowStart = predictedStart.minusDays(2);
        LocalDate windowEnd   = predictedStart.plusDays(2);

        // Fertile window: ovulation typically 14 days before next period
        LocalDate fertileStart = predictedStart.minusDays(17);
        LocalDate fertileEnd   = predictedStart.minusDays(12);

        int confidence = lengths.isEmpty() ? 50 : 70;

        String message = lengths.isEmpty()
                ? "Based on an average 28-day cycle. Log more periods for personalised predictions."
                : String.format(
                "Based on your average cycle of %d days. "
                + "Log more periods for AI-powered predictions.",
                avgLength);

        return PredictionDto.PredictionResponse.builder()
                .predictedStart(predictedStart)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .confidence(confidence)
                .method("MATH")
                .message(message)
                .fertileWindowStart(fertileStart)
                .fertileWindowEnd(fertileEnd)
                .build();
    }

    // ── Level 2: Call Python AI service ─────────────────────────────────────
    @SuppressWarnings("unchecked")
    private PredictionDto.PredictionResponse aiPrediction(
            LocalDate lastStart, List<Integer> lengths) {

        Map<String, Object> requestBody = Map.of(
                "cycle_lengths", lengths,
                "last_start_date", lastStart.toString()
        );

        // Updated API call using RestClient WITH the secret header
        Map<String, Object> aiResponse = restClient.post()
                .uri(aiServiceUrl + "/predict")
                .header("x-internal-secret", aiServiceSecret) // <-- FIX APPLIED HERE
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (aiResponse == null) {
            throw new RuntimeException("Empty response from AI service");
        }

        LocalDate predictedStart = LocalDate.parse(
                aiResponse.get("predicted_start").toString());
        LocalDate windowStart = LocalDate.parse(
                aiResponse.get("window_start").toString());
        LocalDate windowEnd = LocalDate.parse(
                aiResponse.get("window_end").toString());
        int confidence = ((Number) aiResponse.get("confidence")).intValue();
        String message = aiResponse.get("message").toString();

        LocalDate fertileStart = predictedStart.minusDays(17);
        LocalDate fertileEnd   = predictedStart.minusDays(12);

        return PredictionDto.PredictionResponse.builder()
                .predictedStart(predictedStart)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .confidence(confidence)
                .method("AI")
                .message(message)
                .fertileWindowStart(fertileStart)
                .fertileWindowEnd(fertileEnd)
                .build();
    }

    private PredictionDto.PredictionResponse noDataResponse() {
        return PredictionDto.PredictionResponse.builder()
                .confidence(0)
                .method("NONE")
                .message("No cycle data yet. Log your first period to get predictions.")
                .build();
    }
}