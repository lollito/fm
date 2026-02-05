package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "training_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class TrainingPlan implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    @JsonIgnore
    private Team team;

    @Enumerated(EnumType.STRING)
    private TrainingFocus mondayFocus;

    @Enumerated(EnumType.STRING)
    private TrainingFocus tuesdayFocus;

    @Enumerated(EnumType.STRING)
    private TrainingFocus wednesdayFocus;

    @Enumerated(EnumType.STRING)
    private TrainingFocus thursdayFocus;

    @Enumerated(EnumType.STRING)
    private TrainingFocus fridayFocus;

    @Enumerated(EnumType.STRING)
    private TrainingFocus saturdayFocus;

    @Enumerated(EnumType.STRING)
    private TrainingFocus sundayFocus;

    @Enumerated(EnumType.STRING)
    private TrainingIntensity intensity;

    private Boolean restOnWeekends;

    private LocalDateTime lastUpdated;
}
