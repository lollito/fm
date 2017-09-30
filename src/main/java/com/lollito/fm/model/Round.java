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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "round")
public class Round implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private Integer number;
	
	@OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Match> matches = new ArrayList<>();
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "season_id" )
	@JsonIgnore
    private Season season;
	
	public Round() {
		
	}
	
	public Round(Integer number) {
		this.number = number;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Integer getNumber() {
		return number;
	}
	
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	public List<Match> getMatches() {
		return matches;
	}
	
	public void setMatches(List<Match> matches) {
		this.matches = matches;
	}
	
	public void addMatch(Match match){
		match.setRound(this);
		this.matches.add(match);
	}
	
	public Season getSeason() {
		return season;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Round)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Round other = (Round) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("number", number)
				.append("matches", matches)
				.toString();
	}
}
