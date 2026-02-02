package com.lollito.fm.model;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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
	
	private Integer homeShots = 0;
	private Integer homeOnTarget = 0;
	private Integer homeFouls = 0;
	private Integer homeYellowCards = 0;
	private Integer homePossession = 0;
	private Integer homePasses = 0;
	private Integer homeCompletedPasses = 0;
	private Integer homeTackles = 0;
	private Integer homeInterceptions = 0;

	private Integer awayShots = 0;
	private Integer awayOnTarget = 0;
	private Integer awayFouls = 0;
	private Integer awayYellowCards = 0;
	private Integer awayPossession = 0;
	private Integer awayPasses = 0;
	private Integer awayCompletedPasses = 0;
	private Integer awayTackles = 0;
	private Integer awayInterceptions = 0;
	
	
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

	public void addHomeShot() {
		this.homeShots += 1;
	}
	
	@Transient
	public Integer getHomeShotsPerc() {
		return homeShots + awayShots == 0 ? 50 : homeShots * 100 / (homeShots + awayShots);
	}
	
	public Integer getHomeOnTarget() {
		return homeOnTarget;
	}

	public void setHomeOnTarget(Integer onTarget) {
		this.homeOnTarget = onTarget;
	}

	public void addHomeOnTarget() {
		this.homeOnTarget += 1;
	}
	
	@Transient
	public Integer getHomeOnTargetPerc() {
		return homeOnTarget + awayOnTarget == 0 ? 50 : homeOnTarget * 100 / (homeOnTarget + awayOnTarget);
	}
	
	public Integer getHomeFouls() {
		return homeFouls;
	}

	public void setHomeFouls(Integer fouls) {
		this.homeFouls = fouls;
	}

	public void addHomeFoul() {
		this.homeFouls += 1;
	}
	
	@Transient
	public Integer getHomeFoulsPerc() {
		return homeFouls + awayFouls == 0 ? 50 : homeFouls * 100 / (homeFouls + awayFouls);
	}
	
	public Integer getHomeYellowCards() {
		return homeYellowCards;
	}

	public void setHomeYellowCards(Integer yellowCards) {
		this.homeYellowCards = yellowCards;
	}

	public void addHomeYellowCard() {
		this.homeYellowCards += 1;
	}
	
	@Transient
	public Integer getHomeYellowCardsPerc() {
		return homeYellowCards + awayYellowCards == 0 ? 50 : homeYellowCards * 100 / (homeYellowCards + awayYellowCards);
	}
	
	public Integer getHomePossession() {
		return homePossession;
	}

	public void setHomePossession(Integer possession) {
		this.homePossession = possession;
	}

	public Integer getHomePasses() {
		return homePasses;
	}

	public void setHomePasses(Integer homePasses) {
		this.homePasses = homePasses;
	}

	public Integer getHomeCompletedPasses() {
		return homeCompletedPasses;
	}

	public void setHomeCompletedPasses(Integer homeCompletedPasses) {
		this.homeCompletedPasses = homeCompletedPasses;
	}

	public Integer getHomeTackles() {
		return homeTackles;
	}

	public void setHomeTackles(Integer homeTackles) {
		this.homeTackles = homeTackles;
	}

	public Integer getHomeInterceptions() {
		return homeInterceptions;
	}

	public void setHomeInterceptions(Integer homeInterceptions) {
		this.homeInterceptions = homeInterceptions;
	}

	public Integer getAwayShots() {
		return awayShots;
	}

	public void setAwayShots(Integer awayShots) {
		this.awayShots = awayShots;
	}

	public void addAwayShot() {
		this.awayShots += 1;
	}
	
	public Integer getAwayOnTarget() {
		return awayOnTarget;
	}

	public void setAwayOnTarget(Integer awayOnTarget) {
		this.awayOnTarget = awayOnTarget;
	}

	public void addAwayOnTarget() {
		this.awayOnTarget += 1;
	}
	
	public Integer getAwayFouls() {
		return awayFouls;
	}

	public void setAwayFouls(Integer awayFouls) {
		this.awayFouls = awayFouls;
	}

	public void addAwayFoul() {
		this.awayFouls += 1;
	}
	
	public Integer getAwayYellowCards() {
		return awayYellowCards;
	}

	public void setAwayYellowCards(Integer awayYellowCards) {
		this.awayYellowCards = awayYellowCards;
	}

	public void addAwayYellowCard() {
		this.awayYellowCards += 1;
	}
	
	public Integer getAwayPossession() {
		return awayPossession;
	}

	public void setAwayPossession(Integer awayPossession) {
		this.awayPossession = awayPossession;
	}

	public Integer getAwayPasses() {
		return awayPasses;
	}

	public void setAwayPasses(Integer awayPasses) {
		this.awayPasses = awayPasses;
	}

	public Integer getAwayCompletedPasses() {
		return awayCompletedPasses;
	}

	public void setAwayCompletedPasses(Integer awayCompletedPasses) {
		this.awayCompletedPasses = awayCompletedPasses;
	}

	public Integer getAwayTackles() {
		return awayTackles;
	}

	public void setAwayTackles(Integer awayTackles) {
		this.awayTackles = awayTackles;
	}

	public Integer getAwayInterceptions() {
		return awayInterceptions;
	}

	public void setAwayInterceptions(Integer awayInterceptions) {
		this.awayInterceptions = awayInterceptions;
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
