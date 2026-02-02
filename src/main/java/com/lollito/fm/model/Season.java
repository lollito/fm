package com.lollito.fm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "season")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Season implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	private String name;
	
	@OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@Builder.Default
	@ToString.Exclude
	private List<Round> rounds = new ArrayList<>();
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "league_id" )
	@ToString.Exclude
    private League league;
	
	@OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@OrderBy("points desc")
	@Builder.Default
	@ToString.Exclude
	private List<Ranking> rankingLines = new ArrayList<>();
	
	@Builder.Default
	private Integer nextRoundNumber = 1;
	
	public Season(String name) {
		this();
		this.name = name;
	}

	@Transient
	public void addRound(Round round) {
		round.setSeason(this);
		this.rounds.add(round);
	}
	
	public void addRankingLine(Ranking rankingLine) {
		rankingLine.setSeason(this);
		this.rankingLines.add(rankingLine);
	}

}
