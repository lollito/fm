package com.lollito.fm.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_training_focus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PlayerTrainingFocus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @ToString.Exclude
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IndividualTrainingFocus focus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingIntensity intensity;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;
}
