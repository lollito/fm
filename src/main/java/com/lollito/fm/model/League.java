package com.lollito.fm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "league")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class League implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	private String name;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "country_id" )
	@ToString.Exclude
	private Country country;
	
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "server_id" )
    @ToString.Exclude
    private Server server;

	@OneToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL )
	@JoinColumn( name = "season_id" )
    @JsonIgnore
	@ToString.Exclude
    private Season currentSeason;
    
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
	@Builder.Default
	@ToString.Exclude
    private List<Season> seasonHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
	@Builder.Default
	@ToString.Exclude
    private List<Club> clubs = new ArrayList<>();
	
    @OneToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL )
	@JoinColumn( name = "minor_league_id" )
    @JsonIgnore
	@ToString.Exclude
    private League minorLeague;
    
    private Integer promotion;
    
    private Integer relegation;
    
    private Integer euroCup;
    
	public void addSeasonHistory(Season season){
		season.setLeague(this);
		this.seasonHistory.add(season);
	}

}
