package com.lunacycle.service;

import com.lunacycle.dto.SymptomDto;
import com.lunacycle.model.Symptom;
import com.lunacycle.model.User;
import com.lunacycle.repository.SymptomRepository;
import com.lunacycle.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SymptomService {

    private final SymptomRepository symptomRepository;
    private final UserRepository userRepository;

    @Transactional
    public SymptomDto.SymptomResponse logSymptom(UUID userId,
                                                 SymptomDto.LogSymptomRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Symptom symptom = Symptom.builder()
                .user(user)
                .loggedDate(request.getLoggedDate())
                .type(request.getType())
                .severity(request.getSeverity())
                .notes(request.getNotes())
                .build();

        return mapToResponse(symptomRepository.save(symptom));
    }

    public List<SymptomDto.SymptomResponse> getSymptomsForDate(
            UUID userId, LocalDate date) {
        return symptomRepository
                .findByUserIdAndLoggedDateOrderByCreatedAtDesc(userId, date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SymptomDto.SymptomResponse> getSymptomsForRange(
            UUID userId, LocalDate start, LocalDate end) {
        return symptomRepository
                .findByUserIdAndLoggedDateBetweenOrderByLoggedDateDesc(
                        userId, start, end)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSymptom(UUID symptomId, UUID userId) {
        symptomRepository.deleteByIdAndUserId(symptomId, userId);
    }

    private SymptomDto.SymptomResponse mapToResponse(Symptom symptom) {
        return SymptomDto.SymptomResponse.builder()
                .id(symptom.getId())
                .loggedDate(symptom.getLoggedDate())
                .type(symptom.getType())
                .severity(symptom.getSeverity())
                .notes(symptom.getNotes())
                .build();
    }
}