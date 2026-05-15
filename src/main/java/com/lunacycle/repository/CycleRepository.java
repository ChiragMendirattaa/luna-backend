package com.lunacycle.repository;

import com.lunacycle.model.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, UUID> {

    List<Cycle> findByUserIdOrderByStartDateDesc(UUID userId);

    long countByUserId(UUID userId);
}