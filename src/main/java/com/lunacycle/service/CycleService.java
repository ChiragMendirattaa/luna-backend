package com.lunacycle.service;

import com.lunacycle.dto.CycleDto;
import com.lunacycle.model.Cycle;
import com.lunacycle.model.User;
import com.lunacycle.repository.CycleRepository;
import com.lunacycle.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CycleService {

    private final CycleRepository cycleRepository;
    private final UserRepository userRepository;

    @Transactional
    public CycleDto.CycleResponse logCycle(UUID userId,
                                           CycleDto.LogCycleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Calculate cycle length by comparing with most recent previous cycle
        List<Cycle> existing = cycleRepository
                .findByUserIdOrderByStartDateDesc(userId);

        Integer cycleLengthDays = null;
        if (!existing.isEmpty()) {
            Cycle previous = existing.get(0);
            cycleLengthDays = (int) ChronoUnit.DAYS.between(
                    previous.getStartDate(), request.getStartDate());
        }

        Cycle cycle = Cycle.builder()
                .user(user)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .cycleLengthDays(cycleLengthDays)
                .onboardingSeed(request.isOnboardingSeed())
                .notes(request.getNotes())
                .build();

        Cycle saved = cycleRepository.save(cycle);

        // Update user's rolling average cycle length
        updateUserAverageCycleLength(user, existing);

        return mapToResponse(saved);
    }

    public List<CycleDto.CycleResponse> getCycles(UUID userId) {
        return cycleRepository
                .findByUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCycle(UUID cycleId, UUID userId) {
        Cycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new EntityNotFoundException("Cycle not found"));

        if (!cycle.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        cycleRepository.delete(cycle);
    }

    // Rolling average of last 3 cycles — feeds the prediction engine
    private void updateUserAverageCycleLength(User user, List<Cycle> existingCycles) {
        List<Integer> lengths = existingCycles.stream()
                .filter(c -> c.getCycleLengthDays() != null)
                .map(Cycle::getCycleLengthDays)
                .limit(3)
                .collect(Collectors.toList());

        if (!lengths.isEmpty()) {
            int avg = (int) lengths.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(28);
            user.setAverageCycleLength(avg);
            userRepository.save(user);
        }
    }

    private CycleDto.CycleResponse mapToResponse(Cycle cycle) {
        return CycleDto.CycleResponse.builder()
                .id(cycle.getId())
                .startDate(cycle.getStartDate())
                .endDate(cycle.getEndDate())
                .cycleLengthDays(cycle.getCycleLengthDays())
                .onboardingSeed(cycle.isOnboardingSeed())
                .notes(cycle.getNotes())
                .build();
    }
}