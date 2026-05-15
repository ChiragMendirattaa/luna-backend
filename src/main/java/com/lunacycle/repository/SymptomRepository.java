package com.lunacycle.repository;

import com.lunacycle.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, UUID> {

    List<Symptom> findByUserIdAndLoggedDateOrderByCreatedAtDesc(
            UUID userId, LocalDate loggedDate);

    List<Symptom> findByUserIdAndLoggedDateBetweenOrderByLoggedDateDesc(
            UUID userId, LocalDate start, LocalDate end);

    void deleteByIdAndUserId(UUID id, UUID userId);
}