package com.lollito.fm.model;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "player_season_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PlayerSeasonStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @ToString.Exclude
    @JsonIgnore
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    @ToString.Exclude
    @JsonIgnore
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    @JsonIgnore
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    @ToString.Exclude
    @JsonIgnore
    private League league;

    // Match statistics
    @Builder.Default
    private Integer matchesPlayed = 0;
    @Builder.Default
    private Integer matchesStarted = 0;
    @Builder.Default
    private Integer minutesPlayed = 0;
    @Builder.Default
    private Integer substitutionsIn = 0;
    @Builder.Default
    private Integer substitutionsOut = 0;

    // Performance statistics
    @Builder.Default
    private Integer goals = 0;
    @Builder.Default
    private Integer assists = 0;
    @Builder.Default
    private Integer yellowCards = 0;
    @Builder.Default
    private Integer redCards = 0;
    @Builder.Default
    private Integer cleanSheets = 0; // For goalkeepers and defenders

    // Advanced statistics
    @Builder.Default
    private Integer shots = 0;
    @Builder.Default
    private Integer shotsOnTarget = 0;
    @Builder.Default
    private Integer passes = 0;
    @Builder.Default
    private Integer passesCompleted = 0;
    @Builder.Default
    private Integer tackles = 0;
    @Builder.Default
    private Integer interceptions = 0;
    @Builder.Default
    private Integer foulsCommitted = 0;
    @Builder.Default
    private Integer foulsReceived = 0;

    // Goalkeeper specific statistics
    @Builder.Default
    private Integer saves = 0;
    @Builder.Default
    private Integer goalsConceded = 0;
    @Builder.Default
    private Integer penaltiesSaved = 0;

    // Rating and performance
    @Builder.Default
    private Double averageRating = 0.0;
    private Double highestRating;
    private Double lowestRating;
    @Builder.Default
    private Integer manOfTheMatchAwards = 0;

    // Physical statistics
    private Double averageCondition;
    private Double averageMorale;
    @Builder.Default
    private Integer injuryDays = 0;
    @Builder.Default
    private Integer injuryCount = 0;
}
