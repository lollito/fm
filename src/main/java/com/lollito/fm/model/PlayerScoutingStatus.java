package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.hibernate.annotations.GenericGenerator;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "player_scouting_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PlayerScoutingStatus implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club scoutingClub;

    @Enumerated(EnumType.STRING)
    private ScoutingLevel scoutingLevel; // UNKNOWN, BASIC, DETAILED, COMPREHENSIVE

    private LocalDate lastScoutedDate;
    private LocalDate firstScoutedDate;

    private Integer timesScoutedThisSeason;
    private Double knowledgeAccuracy; // 0.0-1.0, how accurate the club's knowledge is

    // Cached revealed attributes (best known values)
    private Double knownStamina;
    private Double knownPlaymaking;
    private Double knownScoring;
    private Double knownWinger;
    private Double knownGoalkeeping;
    private Double knownPassing;
    private Double knownDefending;
    private Double knownSetPieces;
}
