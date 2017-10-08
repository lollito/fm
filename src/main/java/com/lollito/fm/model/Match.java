package com.lollito.fm.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "matchh")
public class Match implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "home_id" )
	private Club home;	
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "away_id" )
	private Club away;
	
	private Integer homeScore = 0;
	
	private Integer awayScore = 0;
	
	private LocalDate date;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "round_id" )
	@JsonIgnore
	private Round round;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "game_id" )
	@JsonIgnore
	private Game game;
	
	@Type(type = "yes_no")
	private Boolean finish = Boolean.FALSE;
	
	public Match() {
		
	}
	
	public Match(Club home, Club away, Game game) {
		this.home = home;
		this.away = away;
		this.game = game;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Club getHome() {
		return home;
	}
	
	public void setHome(Club home) {
		this.home = home;
	}
	
	public Club getAway() {
		return away;
	}
	
	public void setAway(Club away) {
		this.away = away;
	}
	
	public Integer getHomeScore() {
		return homeScore;
	}
	
	public void setHomeScore(Integer homeScore) {
		this.homeScore = homeScore;
	}
	
	public Integer getAwayScore() {
		return awayScore;
	}
	
	public void setAwayScore(Integer awayScore) {
		this.awayScore = awayScore;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public Round getRound() {
		return round;
	}

	public void setRound(Round round) {
		this.round = round;
	}
	
	@Transient
	public Integer getNumber() {
		return round.getNumber();
	}
	
	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Boolean getFinish() {
		return finish;
	}

	public void setFinish(Boolean finish) {
		this.finish = finish;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Match)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Match other = (Match) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat();
	    String dataStr = sdf.format(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("home", home)
				.append("away", away)
				.append("homeScore", homeScore)
				.append("awayScore", awayScore)
				.append("date", dataStr)
				.toString();
	}

}
