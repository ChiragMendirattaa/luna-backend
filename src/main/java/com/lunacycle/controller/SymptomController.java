package com.lunacycle.controller;

import com.lunacycle.dto.SymptomDto;
import com.lunacycle.service.SymptomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService symptomService;

    @PostMapping
    public ResponseEntity<SymptomDto.SymptomResponse> logSymptom(
            Authentication auth,
            @Valid @RequestBody SymptomDto.LogSymptomRequest request) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(symptomService.logSymptom(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<SymptomDto.SymptomResponse>> getSymptoms(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(
                symptomService.getSymptomsForDate(userId, date));
    }

    @GetMapping("/range")
    public ResponseEntity<List<SymptomDto.SymptomResponse>> getSymptomsInRange(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(
                symptomService.getSymptomsForRange(userId, start, end));
    }

    @DeleteMapping("/{symptomId}")
    public ResponseEntity<Void> deleteSymptom(
            Authentication auth,
            @PathVariable UUID symptomId) {
        UUID userId = (UUID) auth.getPrincipal();
        symptomService.deleteSymptom(symptomId, userId);
        return ResponseEntity.noContent().build();
    }
}