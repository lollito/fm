package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDateTime;
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "matchh")
public class Match implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "home_id" )
	private Club home;	
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "away_id" )
	private Club away;
	
	private Integer homeScore = 0;
	
	private Integer awayScore = 0;
	
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
	private LocalDateTime date;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "round_id" )
	@JsonIgnore
	private Round round;
	
	@jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	private Boolean finish = Boolean.FALSE;
	
	@jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	private Boolean last = Boolean.FALSE;
	
	public Integer spectators;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JoinColumn( name = "match_id" )
	@OrderBy("minute")
    private List<EventHistory> events = new ArrayList<>();
    
	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL )
    @JoinColumn( name = "match_id" )
    private Stats stats;
	
	public Match() {
		
	}
	
	public Match(Club home, Club away, Boolean last) {
		this.home = home;
		this.away = away;
		this.last = last;
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

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
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
	
	public Boolean getFinish() {
		return finish;
	}

	public void setFinish(Boolean finish) {
		this.finish = finish;
	}

	public Boolean getLast() {
		return last;
	}

	public void setLast(Boolean last) {
		this.last = last;
	}
	
	public Integer getSpectators() {
		return spectators;
	}

	public void setSpectators(Integer spectators) {
		this.spectators = spectators;
	}

	public List<EventHistory> getEvents() {
		return events;
	}

	public void setEvents(List<EventHistory> events) {
		this.events = events;
	}

	public void addEvents(List<EventHistory> events) {
		this.events.addAll(events);
	}
	
	public void addEvent(EventHistory event) {
		this.events.add(event);
	}
	
	public void removeEvent(EventHistory event) {
		this.events.remove(event);
	}
	
	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
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
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("home", home)
				.append("away", away)
				.append("homeScore", homeScore)
				.append("awayScore", awayScore)
				.append("date", date)
				.toString();
	}

}
