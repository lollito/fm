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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="ranking")
public class Ranking implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "club_id" )
	private Club club ;
	
	private Integer played = 0;
	
	private Integer points = 0;
	
	private Integer won = 0;
	
	private Integer drawn = 0;
	
	private Integer lost = 0;

	private Integer goalsFor = 0;
	
	private Integer goalAgainst = 0;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "season_id" )
	@JsonIgnore
	private Season season;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Club getClub() {
		return club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

	public Integer getPlayed() {
		return played;
	}

	public void setPlayed(Integer played) {
		this.played = played;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public Integer getWon() {
		return won;
	}

	public void setWon(Integer won) {
		this.won = won;
	}

	public Integer getDrawn() {
		return drawn;
	}

	public void setDrawn(Integer drawn) {
		this.drawn = drawn;
	}

	public Integer getLost() {
		return lost;
	}

	public void setLost(Integer lost) {
		this.lost = lost;
	}

	public Integer getGoalsFor() {
		return goalsFor;
	}

	public void setGoalsFor(Integer goalsFor) {
		this.goalsFor = goalsFor;
	}

	public Integer getGoalAgainst() {
		return goalAgainst;
	}

	public void setGoalAgainst(Integer goalAgainst) {
		this.goalAgainst = goalAgainst;
	}

	public Season getSeason() {
		return season;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	@Transient
	public void updateStats(Integer goalsFor, Integer goalAgainst){
		if(goalsFor > goalAgainst){
			addWon();
		} else if(goalsFor < goalAgainst){
			addLost();
		} else {
			addDrawn();
		}
		this.goalsFor += goalsFor;
		this.goalAgainst += goalAgainst;
		this.played += 1;
	}
	
	@Transient
	private void addWon(){
		this.won += 1;
		this.points += 3;
	}
	
	@Transient
	private void addDrawn(){
		this.drawn += 1;
		this.points += 1;
	}
	
	@Transient
	private void addLost(){
		this.lost += 1;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Ranking)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Ranking other = (Ranking) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
}
