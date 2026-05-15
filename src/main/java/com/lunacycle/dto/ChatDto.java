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
    }

    @Data
    public static class SymptomEntry {
        private String date;
        private String type;
        private Integer severity;
    }

    @Data
    @Builder
    public static class ChatResponse {
        private String reply;
    }
}