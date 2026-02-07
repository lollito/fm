package com.lollito.fm.model;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
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
@Table(name = "match_player_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class MatchPlayerStats implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    @JsonIgnore
    @ToString.Exclude
    private Match match;

    @ManyToOne
    @ToString.Exclude
    private Player player;

    @Builder.Default
    private Integer goals = 0;
    @Builder.Default
    private Integer assists = 0;
    @Builder.Default
    private Integer yellowCards = 0;
    @Builder.Default
    private Integer redCards = 0;
    @Builder.Default
    private Integer shots = 0;
    @Builder.Default
    private Integer shotsOnTarget = 0;
    @Builder.Default
    private Integer passes = 0;
    @Builder.Default
    private Integer completedPasses = 0;
    @Builder.Default
    private Integer tackles = 0;
    @Builder.Default
    private Double rating = 6.0;
    @Builder.Default
    private Boolean mvp = false;
    private String position;

    @Builder.Default
    private Integer minutesPlayed = 0;
    @Builder.Default
    private Integer saves = 0;
    @Builder.Default
    private Integer goalsConceded = 0;
    @Builder.Default
    private Integer penaltiesSaved = 0;

    @Builder.Default
    private boolean started = false;

    public boolean isStarted() {
        return started;
    }
}
