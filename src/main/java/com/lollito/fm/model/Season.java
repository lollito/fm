package com.lollito.fm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "season")
public class Season implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	private String name;
	
	@OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	private List<Round> rounds = new ArrayList<>();
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "league_id" )
    private League league;
	
	@OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@OrderBy("points desc")
	private List<Ranking> rankingLines = new ArrayList<>();
	
	private Integer nextRoundNumber = 1;
	
	public Season() {
		
	}
	
	public Season(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Round> getRounds() {
		return rounds;
	}
	
	public void setRounds(List<Round> rounds) {
		this.rounds = rounds;
	}
	
	@Transient
	public void addRound(Round round) {
		round.setSeason(this);
		this.rounds.add(round);
	}
	
	public League getLeague() {
		return league;
	}

	public void setLeague(League league) {
		this.league = league;
	}

	public List<Ranking> getRankingLines() {
		return rankingLines;
	}

	public void setRankingLines(List<Ranking> rankingLines) {
		this.rankingLines = rankingLines;
	}

	public void addRankingLine(Ranking rankingLine) {
		rankingLine.setSeason(this);
		this.rankingLines.add(rankingLine);
	}

	public Integer getNextRoundNumber() {
		return nextRoundNumber;
	}

	public void setNextRoundNumber(Integer currentRoundNumber) {
		this.nextRoundNumber = currentRoundNumber;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Season)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Season other = (Season) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("name", name)
				.append("rounds", rounds)
				.toString();
	}
}
