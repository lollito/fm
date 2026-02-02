package com.lollito.fm.model;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Stats implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	@Builder.Default
	private Integer homeShots = 0;
	@Builder.Default
	private Integer homeOnTarget = 0;
	@Builder.Default
	private Integer homeFouls = 0;
	@Builder.Default
	private Integer homeYellowCards = 0;
	@Builder.Default
	private Integer homePossession = 0;
	@Builder.Default
	private Integer homePasses = 0;
	@Builder.Default
	private Integer homeCompletedPasses = 0;
	@Builder.Default
	private Integer homeTackles = 0;
	@Builder.Default
	private Integer homeInterceptions = 0;

	@Builder.Default
	private Integer awayShots = 0;
	@Builder.Default
	private Integer awayOnTarget = 0;
	@Builder.Default
	private Integer awayFouls = 0;
	@Builder.Default
	private Integer awayYellowCards = 0;
	@Builder.Default
	private Integer awayPossession = 0;
	@Builder.Default
	private Integer awayPasses = 0;
	@Builder.Default
	private Integer awayCompletedPasses = 0;
	@Builder.Default
	private Integer awayTackles = 0;
	@Builder.Default
	private Integer awayInterceptions = 0;
	
	public void addHomeShot() {
		this.homeShots += 1;
	}
	
	@Transient
	public Integer getHomeShotsPerc() {
		return homeShots + awayShots == 0 ? 50 : homeShots * 100 / (homeShots + awayShots);
	}
	
	public void addHomeOnTarget() {
		this.homeOnTarget += 1;
	}
	
	@Transient
	public Integer getHomeOnTargetPerc() {
		return homeOnTarget + awayOnTarget == 0 ? 50 : homeOnTarget * 100 / (homeOnTarget + awayOnTarget);
	}
	
	public void addHomeFoul() {
		this.homeFouls += 1;
	}
	
	@Transient
	public Integer getHomeFoulsPerc() {
		return homeFouls + awayFouls == 0 ? 50 : homeFouls * 100 / (homeFouls + awayFouls);
	}
	
	public void addHomeYellowCard() {
		this.homeYellowCards += 1;
	}
	
	@Transient
	public Integer getHomeYellowCardsPerc() {
		return homeYellowCards + awayYellowCards == 0 ? 50 : homeYellowCards * 100 / (homeYellowCards + awayYellowCards);
	}
	
	public void addAwayShot() {
		this.awayShots += 1;
	}
	
	public void addAwayOnTarget() {
		this.awayOnTarget += 1;
	}
	
	public void addAwayFoul() {
		this.awayFouls += 1;
	}
	
	public void addAwayYellowCard() {
		this.awayYellowCards += 1;
	}
	
}
