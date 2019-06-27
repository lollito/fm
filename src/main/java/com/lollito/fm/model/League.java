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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "league")
public class League implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	private String name;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "country_id" )
	private Country country;
	
	@OneToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL )
	@JoinColumn( name = "season_id" )
    @JsonIgnore
    private Season currentSeason;
    
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Season> seasonHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Club> clubs = new ArrayList<>();
	
    @OneToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL )
	@JoinColumn( name = "league_id" )
    @JsonIgnore
    private League minorLeague;
    
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

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Season getCurrentSeason() {
		return currentSeason;
	}

	public void setCurrentSeason(Season currentSeason) {
		this.currentSeason = currentSeason;
	}

	public List<Season> getSeasonHistory() {
		return seasonHistory;
	}

	public void setSeasonHistory(List<Season> seasonHistory) {
		this.seasonHistory = seasonHistory;
	}

	public void addSeasonHistory(Season season){
		season.setLeague(this);
		this.seasonHistory.add(season);
	}

	public List<Club> getClubs() {
		return clubs;
	}

	public void setClubs(List<Club> clubs) {
		this.clubs = clubs;
	}

	public League getMinorLeague() {
		return minorLeague;
	}

	public void setMinorLeague(League minorLeague) {
		this.minorLeague = minorLeague;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof League)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			League other = (League) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("id", id)
				.append("country", country)
				.toString();
	}
}
