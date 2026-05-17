package com.lunacycle.service;

import com.lunacycle.dto.ChatDto;
import com.lunacycle.repository.CycleRepository;
import com.lunacycle.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RestClient restClient;
    private final CycleRepository cycleRepository;
    private final SymptomRepository symptomRepository;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Value("${ai.service.secret}")
    private String aiServiceSecret;

    public ChatDto.ChatResponse chat(UUID userId, ChatDto.ChatRequest request) {

        // Build cycle summary from user's data
        var cycles = cycleRepository.findByUserIdOrderByStartDateDesc(userId);

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
                        "severity", s.getSeverity() != null ? s.getSeverity() : 2
                ))
                .collect(Collectors.toList());

        // Build payload — include conversation history if provided
        List<Map<String, String>> conversationHistory = new ArrayList<>();
        if (request.getConversationHistory() != null) {
            // Take last 10 messages max to avoid token bloat
            List<ChatDto.ConversationMessage> hist = request.getConversationHistory();
            int start = Math.max(0, hist.size() - 10);
            for (ChatDto.ConversationMessage msg : hist.subList(start, hist.size())) {
                conversationHistory.add(Map.of(
                        "role",    msg.getRole(),
                        "content", msg.getContent()
                ));
            }
        }

        Map<String, Object> payload = Map.of(
                "message",              request.getMessage(),
                "cycle_summary",        cycleSummary,
                "symptom_history",      symptomHistory,
                "conversation_history", conversationHistory
        );

        try {
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
