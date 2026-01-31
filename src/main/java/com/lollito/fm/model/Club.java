package com.lollito.fm.model;
import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "club")
public class Club implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
    private String name;
    
    private LocalDate foundation;
    
    private String logoURL;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "team_id" )
    @JsonIgnore
    private Team team;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "under18_id" )
    @JsonIgnore
    private Team under18;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "stadium_id" )
    @JsonIgnore
    private Stadium stadium;
    
    @ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "league_id" )
    @JsonIgnore
    private League league;
    
    @OneToOne( fetch = FetchType.LAZY, mappedBy="club" )
    @JsonIgnore
    private User user;
    
    @ManyToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "finance_id" )
//    @JsonIgnore
	private Finance finance;
    
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
	
	public LocalDate getFoundation() {
		return foundation;
	}
	
	public void setFoundation(LocalDate foundation) {
		this.foundation = foundation;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public void setTeam(Team team) {
		this.team = team;
	}
	
	public Team getUnder18() {
		return under18;
	}

	public void setUnder18(Team under18) {
		this.under18 = under18;
	}

	public Stadium getStadium() {
		return stadium;
	}
	
	public void setStadium(Stadium stadium) {
		this.stadium = stadium;
	}

	public League getLeague() {
		return league;
	}

	public void setLeague(League league) {
		this.league = league;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Finance getFinance() {
		return finance;
	}

	public void setFinance(Finance finance) {
		this.finance = finance;
	}

	public String getLogoURL() {
		return logoURL;
	}

	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(name).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Club)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Club other = (Club) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
}
