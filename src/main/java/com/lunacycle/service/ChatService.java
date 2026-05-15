package com.lunacycle.service;

import com.lunacycle.dto.ChatDto;
import com.lunacycle.repository.CycleRepository;
import com.lunacycle.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient; // 1. Swapped to RestClient

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RestClient restClient; // 2. Injected RestClient instead
    private final CycleRepository cycleRepository;
    private final SymptomRepository symptomRepository;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Value("${ai.service.secret}")
    private String aiServiceSecret;

    public ChatDto.ChatResponse chat(UUID userId, ChatDto.ChatRequest request) {

        // Build cycle summary from user's data
        var cycles = cycleRepository.findByUserIdOrderByStartDateDesc(userId);

        // 3. Fixed the double-to-int cast for String.format("%d")
        String cycleSummary = cycles.isEmpty() ? "" :
                String.format("Last period: %s. Average cycle: %d days.",
                        cycles.get(0).getStartDate(),
                        (int) cycles.stream()
                              .filter(c -> c.getCycleLengthDays() != null)
                              .mapToInt(c -> c.getCycleLengthDays())
                              .average()
                              .orElse(28.0));

        // Fetch last 14 days of symptoms for context
        var recentSymptoms = symptomRepository
                .findByUserIdAndLoggedDateBetweenOrderByLoggedDateDesc(
                        userId,
                        LocalDate.now().minusDays(14),
                        LocalDate.now()
                );

        List<Map<String, Object>> symptomHistory = recentSymptoms.stream()
                .map(s -> Map.<String, Object>of(
                        "date",     s.getLoggedDate().toString(),
                        "type",     s.getType().toString(),
                        "severity", s.getSeverity() != null
                                ? s.getSeverity() : 2
                ))
                .collect(Collectors.toList());

        // Forward to Python AI service
        Map<String, Object> payload = Map.of(
                "message",         request.getMessage(),
                "cycle_summary",   cycleSummary,
                "symptom_history", symptomHistory
        );

        try {
            // 4. Updated to use the clean RestClient syntax
            Map<?, ?> response = restClient.post()
                    .uri(aiServiceUrl + "/chat")
                    .header("x-internal-secret", aiServiceSecret)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            String reply = response != null && response.get("reply") != null
                    ? response.get("reply").toString()
                    : "I'm here for you! Could you tell me more?";

            return ChatDto.ChatResponse.builder().reply(reply).build();

        } catch (Exception e) {
            log.warn("Chat AI service error: {}", e.getMessage());
            return ChatDto.ChatResponse.builder()
                    .reply("I'm having a little trouble right now. " +
                            "Please try again in a moment! 🌙")
                    .build();
        }
    }
}