package com.lollito.fm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "performance_bonus")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceBonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    @JsonIgnore
    private Contract contract;

    @Enumerated(EnumType.STRING)
    private BonusType type;

    private String description;
    private String triggerCondition; // JSON condition
    private BigDecimal bonusAmount;
    private Integer targetValue; // Goals, assists, matches, etc.
    private Boolean isAchieved;
    private LocalDate achievedDate;
}
