package com.lunacycle.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "symptoms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Symptom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SymptomType type;

    private Short severity;

    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SymptomType {
        CRAMPS, BLOATING, HEADACHE, FATIGUE, ACNE,
        MOOD_LOW, MOOD_ANXIOUS, MOOD_HAPPY,
        HIGH_ENERGY, LOW_ENERGY,
        BREAST_TENDERNESS, BACK_PAIN, NAUSEA,
        INSOMNIA, APPETITE_HIGH, APPETITE_LOW
    }
}