package com.lunacycle.controller;

import com.lunacycle.dto.CycleDto;
import com.lunacycle.service.CycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
public class CycleController {

    private final CycleService cycleService;

    @PostMapping
    public ResponseEntity<CycleDto.CycleResponse> logCycle(
            Authentication auth,
            @Valid @RequestBody CycleDto.LogCycleRequest request) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cycleService.logCycle(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<CycleDto.CycleResponse>> getCycles(
            Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(cycleService.getCycles(userId));
    }

    @DeleteMapping("/{cycleId}")
    public ResponseEntity<Void> deleteCycle(
            Authentication auth,
            @PathVariable UUID cycleId) {
        UUID userId = (UUID) auth.getPrincipal();
        cycleService.deleteCycle(cycleId, userId);
        return ResponseEntity.noContent().build();
    }
}