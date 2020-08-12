package com.lollito.fm.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "stats")
public class Stats implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	private Integer homeShots;
	private Integer homeOnTarget;
	private Integer homeFouls;
	private Integer homeYellowCards;
	private Integer homePossession;
	
	private Integer awayShots;
	private Integer awayOnTarget;
	private Integer awayFouls;
	private Integer awayYellowCards;
	private Integer awayPossession;
	
	
	public Stats() {
		
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Integer getHomeShots() {
		return homeShots;
	}

	public void setHomeShots(Integer shots) {
		this.homeShots = shots;
	}

	public Integer getHomeOnTarget() {
		return homeOnTarget;
	}

	public void setHomeOnTarget(Integer onTarget) {
		this.homeOnTarget = onTarget;
	}

	public Integer getHomeFouls() {
		return homeFouls;
	}

	public void setHomeFouls(Integer fouls) {
		this.homeFouls = fouls;
	}

	public Integer getHomeYellowCards() {
		return homeYellowCards;
	}

	public void setHomeYellowCards(Integer yellowCards) {
		this.homeYellowCards = yellowCards;
	}

	public Integer getHomePossession() {
		return homePossession;
	}

	public void setHomePossession(Integer possession) {
		this.homePossession = possession;
	}

	public Integer getAwayShots() {
		return awayShots;
	}

	public void setAwayShots(Integer awayShots) {
		this.awayShots = awayShots;
	}

	public Integer getAwayOnTarget() {
		return awayOnTarget;
	}

	public void setAwayOnTarget(Integer awayOnTarget) {
		this.awayOnTarget = awayOnTarget;
	}

	public Integer getAwayFouls() {
		return awayFouls;
	}

	public void setAwayFouls(Integer awayFouls) {
		this.awayFouls = awayFouls;
	}

	public Integer getAwayYellowCards() {
		return awayYellowCards;
	}

	public void setAwayYellowCards(Integer awayYellowCards) {
		this.awayYellowCards = awayYellowCards;
	}

	public Integer getAwayPossession() {
		return awayPossession;
	}

	public void setAwayPossession(Integer awayPossession) {
		this.awayPossession = awayPossession;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Stats)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Stats other = (Stats) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("shots", homeShots)
				.append("onTarget", homeOnTarget)
				.toString();
	}
}
