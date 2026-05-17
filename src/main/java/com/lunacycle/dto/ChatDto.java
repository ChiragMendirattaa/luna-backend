package com.lunacycle.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

public class ChatDto {

    @Data
    public static class ChatRequest {
        private String message;
        private List<SymptomEntry> symptomHistory;
        private String cycleSummary;
        // Rolling conversation history for context (last 5-10 messages)
        private List<ConversationMessage> conversationHistory;
    }

    @Data
    public static class SymptomEntry {
        private String date;
        private String type;
        private Integer severity;
    }

    @Data
    public static class ConversationMessage {
        private String role;    // "user" or "assistant"
        private String content;
    }

    @Data
    @Builder
    public static class ChatResponse {
        private String reply;
    }
}
