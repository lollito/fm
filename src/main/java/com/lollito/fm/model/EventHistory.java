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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "event_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class EventHistory implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    @JsonIgnore
    @ToString.Exclude
    private Match match;

	private String event;
	
	private Integer minute;

	private Event type;
	
	private Integer homeScore;
	private Integer awayScore;

	public EventHistory(String event, Integer minute) {
		this();
		this.event = event;
		this.minute = minute;
	}

	public EventHistory(String event, Integer minute, Event type) {
		this();
		this.event = event;
		this.minute = minute;
		this.type = type;
	}

	public EventHistory(String event, Integer minute, Event type, Integer homeScore, Integer awayScore) {
		this();
		this.event = event;
		this.minute = minute;
		this.type = type;
		this.homeScore = homeScore;
		this.awayScore = awayScore;
	}
}
