package com.lunacycle.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

public class PredictionDto {

    @Data
    @Builder
    public static class PredictionResponse {
        private LocalDate predictedStart;      // single best-guess date
        private LocalDate windowStart;          // earliest possible date
        private LocalDate windowEnd;            // latest possible date
        private int confidence;                 // 0-100 percent
        private String method;                  // "MATH" or "AI"
        private String message;                 // human-readable explanation
        private LocalDate fertileWindowStart;   // ovulation window start
        private LocalDate fertileWindowEnd;     // ovulation window end
    }
}