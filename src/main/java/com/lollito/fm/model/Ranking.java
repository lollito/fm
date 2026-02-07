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
@Table(name="ranking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Ranking implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "club_id" )
	@ToString.Exclude
	private Club club ;
	
	@Builder.Default
	private Integer played = 0;
	
	@Builder.Default
	private Integer points = 0;
	
	@Builder.Default
	private Integer won = 0;
	
	@Builder.Default
	private Integer drawn = 0;
	
	@Builder.Default
	private Integer lost = 0;

	@Builder.Default
	private Integer goalsFor = 0;
	
	@Builder.Default
	private Integer goalAgainst = 0;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "season_id" )
	@JsonIgnore
	@ToString.Exclude
	private Season season;
	
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

}
