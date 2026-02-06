package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "player_career_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PlayerCareerStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @ToString.Exclude
    @JsonIgnore
    private Player player;

    // Career totals
    @Builder.Default
    private Integer totalMatchesPlayed = 0;
    @Builder.Default
    private Integer totalGoals = 0;
    @Builder.Default
    private Integer totalAssists = 0;
    @Builder.Default
    private Integer totalYellowCards = 0;
    @Builder.Default
    private Integer totalRedCards = 0;
    @Builder.Default
    private Integer totalCleanSheets = 0;

    // Career achievements
    @Builder.Default
    private Integer leagueTitles = 0;
    @Builder.Default
    private Integer cupTitles = 0;
    @Builder.Default
    private Integer internationalCaps = 0;
    @Builder.Default
    private Integer internationalGoals = 0;

    // Career records
    @Builder.Default
    private Integer longestGoalStreak = 0;
    @Builder.Default
    private Integer mostGoalsInSeason = 0;
    @Builder.Default
    private Integer mostAssistsInSeason = 0;
    @Builder.Default
    private Double highestSeasonRating = 0.0;

    // Career milestones
    private LocalDate firstProfessionalMatch;
    private LocalDate firstGoal;
    private LocalDate milestone100Matches;
    private LocalDate milestone100Goals;

    // Transfer history
    @Builder.Default
    private Integer clubsPlayed = 0;
    @Builder.Default
    private BigDecimal totalTransferValue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal highestTransferValue = BigDecimal.ZERO;
}
