package com.lunacycle.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lunacycle.model.User;
import com.lunacycle.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PatchMapping("/me")
    public ResponseEntity<UpdateMeResponse> updateMe(
            Authentication auth,
            @RequestBody UpdateMeRequest request) {

        UUID userId = (UUID) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        log.info("Received update for user {}: cycleLength={}, onboardingDone={}",
                userId, request.getAverageCycleLength(), request.getOnboardingDone());

        if (request.getAverageCycleLength() != null) {
            user.setAverageCycleLength(request.getAverageCycleLength());
        }
        if (request.getOnboardingDone() != null) {
            user.setOnboardingDone(request.getOnboardingDone());
        }

        User saved = userRepository.save(user);

        UpdateMeResponse response = new UpdateMeResponse();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setName(saved.getName());
        response.setAverageCycleLength(saved.getAverageCycleLength());
        response.setOnboardingDone(saved.isOnboardingDone());

        return ResponseEntity.ok(response);
    }

    @Data
    static class UpdateMeRequest {
        private Integer averageCycleLength;
        @JsonProperty("onboardingDone")
        private Boolean onboardingDone;
    }

    @Data
    static class UpdateMeResponse {
        private UUID id;
        private String email;
        private String name;
        private Integer averageCycleLength;
        private boolean onboardingDone;
    }
}