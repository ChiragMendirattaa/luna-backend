package com.lunacycle.dto;

import com.lunacycle.model.Symptom.SymptomType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

public class SymptomDto {

    @Data
    public static class LogSymptomRequest {
        @NotNull(message = "Date is required")
        private LocalDate loggedDate;

        @NotNull(message = "Symptom type is required")
        private SymptomType type;

        @Min(value = 1, message = "Severity minimum is 1")
        @Max(value = 3, message = "Severity maximum is 3")
        private Short severity;

        private String notes;
    }

    @Data
    @Builder
    public static class SymptomResponse {
        private UUID id;
        private LocalDate loggedDate;
        private SymptomType type;
        private Short severity;
        private String notes;
    }
}