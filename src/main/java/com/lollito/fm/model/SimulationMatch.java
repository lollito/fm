package com.lollito.fm.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "simulation_match")
public class SimulationMatch implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "match_id" )
	private Match match;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "home_formation_id" )
	private Formation homeFormation;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "away_formation_id" )
	private Formation awayFormation;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Match getMatch() {
		return match;
	}
	
	public void setMatch(Match match) {
		this.match = match;
	}

	public Formation getHomeFormation() {
		return homeFormation;
	}

	public void setHomeFormation(Formation homeFormation) {
		this.homeFormation = homeFormation;
	}

	public Formation getAwayFormation() {
		return awayFormation;
	}

	public void setAwayFormation(Formation awayFormation) {
		this.awayFormation = awayFormation;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SimulationMatch)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			SimulationMatch other = (SimulationMatch) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
}
