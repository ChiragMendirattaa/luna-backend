package com.lunacycle.controller;

import com.lunacycle.model.User;
import com.lunacycle.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PatchMapping("/me")
    public ResponseEntity<Void> updateMe(
            Authentication auth,
            @RequestBody UpdateMeRequest request) {

        UUID userId = (UUID) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found"));

        if (request.getAverageCycleLength() != null) {
            user.setAverageCycleLength(request.getAverageCycleLength());
        }
        if (request.getOnboardingDone() != null) {
            user.setOnboardingDone(request.getOnboardingDone());
        }

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @Data
    static class UpdateMeRequest {
        private Integer averageCycleLength;
        private Boolean onboardingDone;
    }
}