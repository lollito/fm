package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

@Entity
@Table(name = "matchh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Match implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "home_id" )
	@ToString.Exclude
	private Club home;	
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "away_id" )
	@ToString.Exclude
	private Club away;
	
	@Builder.Default
	private Integer homeScore = 0;
	
	@Builder.Default
	private Integer awayScore = 0;
	
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
	private LocalDateTime date;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "round_id" )
	@JsonIgnore
	@ToString.Exclude
	private Round round;
	
	@jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	@Builder.Default
	private Boolean finish = Boolean.FALSE;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private MatchStatus status = MatchStatus.SCHEDULED;
	
	@jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	@Builder.Default
	private Boolean last = Boolean.FALSE;
	
	public Integer spectators;
	
	@OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("minute")
	@Builder.Default
	@ToString.Exclude
    private List<EventHistory> events = new ArrayList<>();
    
	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL )
    @JoinColumn( name = "match_id" )
	@ToString.Exclude
    private Stats stats;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@ToString.Exclude
	private Formation homeFormation;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@ToString.Exclude
	private Formation awayFormation;

	@OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	private List<MatchPlayerStats> playerStats = new ArrayList<>();
	
	public Match(Club home, Club away, Boolean last) {
		this();
		this.home = home;
		this.away = away;
		this.last = last;
	}

	@Transient
	public Integer getNumber() {
		return round != null ? round.getNumber() : null;
	}

	@Transient
	public String getCompetitionName() {
		if (round != null && round.getSeason() != null && round.getSeason().getLeague() != null) {
			return round.getSeason().getLeague().getName();
		}
		return "League";
	}

	@Transient
	public String getStadiumName() {
		if (home != null && home.getStadium() != null) {
			return home.getStadium().getName();
		}
		return "Unknown Stadium";
	}
	
	public void setFinish(Boolean finish) {
		this.finish = finish;
		if (finish) {
			this.status = MatchStatus.COMPLETED;
		}
	}

	public void addEvents(List<EventHistory> events) {
		events.forEach(e -> e.setMatch(this));
		this.events.addAll(events);
	}
	
	public void addEvent(EventHistory event) {
		event.setMatch(this);
		this.events.add(event);
	}
	
	public void removeEvent(EventHistory event) {
		this.events.remove(event);
	}

}
