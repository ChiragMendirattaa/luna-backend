package com.lunacycle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

public class CycleDto {

    @Data
    public static class LogCycleRequest {
        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        private LocalDate endDate;
        private String notes;
        @JsonProperty("onboardingSeed")
        private boolean onboardingSeed = false;
    }

    @Data
    @Builder
    public static class CycleResponse {
        private UUID id;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer cycleLengthDays;
        private boolean onboardingSeed;
        private String notes;
    }
}