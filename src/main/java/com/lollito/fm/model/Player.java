package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


//Portieri:
//Parate, inutile a dirsi, è importante per i portieri. Quest'ultimi tuttavia si avvantaggiano anche dall'avere una buona abilità in difesa, mentre calci piazzati aiuta contro rigori e calci di punizione.
//
//Difensori centrali:
//I difensori dovrebbero avere molta difesa, ovviamente. È anche importante per loro avere regia, mentre i passaggi fanno la differenza quando si usa la tattica Contropiede.
//
//Terzini:
//Difesa è la skill più importante per loro, ma beneficiano anche molto dalla propria abilità in cross. Contribuiscono un po' anche a centrocampo con Regia, e con i passaggi quando si usa la tattica Contropiede.
//
//Centrocampisti Centrali:
//Avere buoni registi in mezzo al campo è un ingrediente chiave per la maggior parte delle squadre di successo. Essi usano molto anche le loro abilità di passaggio e difesa e non disdegnano l'attacco.
//
//Ali:
//Ovviamente la loro skill principale è cross, ma è importante anche regia. Inoltre fanno buon uso di passaggi e di difesa.
//
//Attaccanti:
//Il loro compito principale è segnare goal, quindi attacco è la loro skill principale. Anche passaggi è molto importante, così come cross e regia.

@Entity
@Table(name = "player")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Player implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	private String name;
	private String surname;
	
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
	private LocalDate birth;
	
//	Decide quanta abilità perde un giocatore durante il corso di una partita, a causa della fatica.
	private Double stamina;
	
//	L'abilità di controllare il gioco e trasformarlo in occasioni da goal. 
	private Double playmaking;
	
// 	L'abilità di mettere la palla in rete.
	private Double scoring;
	
// 	L'abilità di finalizzare azioni da goal grazie alle discese sulle fasce. 
	private Double winger;
	
// 	Evitare che la palla entri nella propria rete. 
	private Double goalkeeping;
	
// 	Giocatori che sanno come fare il passaggio decisivo sono di grande aiuto per l'attacco della squadra. 
	private Double passing;
	
// 	L'abilità di fermare gli attacchi avversari. 
	private Double defending;
	
// 	Il risultato delle punizioni e dei rigori dipende da quanto è abile il tuo specialista in calci piazzati. 
	private Double setPieces;

	@Column(name = "cndtion")
	@Builder.Default
	private Double condition = 100.0;
	
	@Builder.Default
	private Double moral = 100.0;
	
	@Enumerated(EnumType.ORDINAL)
	private PlayerRole role;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "team_id" )
	@JsonIgnore
	@ToString.Exclude
    private Team team;

	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "original_team_id" )
	@JsonIgnore
	@ToString.Exclude
    private Team originalTeam;
	
	private BigDecimal salary;

	@Builder.Default
	private Boolean onSale = Boolean.FALSE;
	
	@Enumerated(EnumType.ORDINAL)
	private Foot preferredFoot;
	
	@OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
	@JsonIgnore
	@ToString.Exclude
	private PlayerCareerStats careerStats;

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	@ToString.Exclude
	private List<PlayerSeasonStats> seasonStats = new ArrayList<>();

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	@ToString.Exclude
	private List<PlayerAchievement> achievements = new ArrayList<>();

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	@ToString.Exclude
	private List<PlayerTransferHistory> transferHistory = new ArrayList<>();

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	@ToString.Exclude
	private List<Injury> injuries = new ArrayList<>();

	@OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
	@JsonIgnore
	@ToString.Exclude
	private Contract currentContract;

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	@ToString.Exclude
	private List<Contract> contractHistory = new ArrayList<>();

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonIgnore
	@ToString.Exclude
	private List<ContractNegotiation> negotiations = new ArrayList<>();

	@Transient
	public boolean isContractExpiringSoon() {
		if (currentContract == null) return true;
		return currentContract.getEndDate().isBefore(LocalDate.now().plusMonths(6));
	}

	@Transient
	public BigDecimal getWeeklySalary() {
		return currentContract != null ? currentContract.getWeeklySalary() : BigDecimal.ZERO;
	}

	@Transient
	public boolean hasReleaseClause() {
		return currentContract != null && currentContract.getHasReleaseClause();
	}

	@Transient
	public boolean isInjured() {
		return injuries.stream()
			.anyMatch(injury -> injury.getStatus() == InjuryStatus.ACTIVE);
	}

	@Transient
	public Double getInjuryPerformanceMultiplier() {
		return injuries.stream()
			.filter(injury -> injury.getStatus() == InjuryStatus.ACTIVE)
			.mapToDouble(Injury::getPerformanceImpact)
			.min()
			.orElse(1.0);
	}

	@Transient
	public PlayerSeasonStats getCurrentSeasonStats() {
		return seasonStats.stream()
			.filter(stats -> stats.getSeason().isCurrent())
			.findFirst()
			.orElse(null);
	}

	@Transient
	public Integer getTotalCareerGoals() {
		return careerStats != null ? careerStats.getTotalGoals() : 0;
	}

	@Transient
	public Integer getTotalCareerMatches() {
		return careerStats != null ? careerStats.getTotalMatchesPlayed() : 0;
	}

	public Player(String name, String surname, LocalDate birth) {
		this();
		this.name = name;
		this.surname = surname;
		this.birth = birth;
	}

	@Transient
	public void decrementCondition(Double decrement) {
		if(this.condition - decrement < 0){
			this.condition = 0.0;
		} else {
			this.condition -= decrement;
		}
	}
	
	@Transient
	public void incrementCondition(Double increment) {
		if(this.condition + increment > 100){
			this.condition = 100.0;
		} else {
			this.condition += increment;
		}
	}
	
	@Transient
	public void decrementMoral(Double decrement) {
		if(this.moral - decrement < 0){
			this.moral = 0.0;
		} else {
			this.moral -= decrement;
		}
	}
	
	@Transient
	public void incrementMoral(Double increment) {
		if(this.moral + increment > 100){
			this.moral = 100.0;
		} else {
			this.moral += increment;
		}
	}
	
	@Transient
	public Integer getAge(){
		return Period.between(birth, LocalDate.now()).getYears();
	}
	
	@Transient
	public void updateSkills(Double update){
		this.stamina += update;
		this.playmaking += update;
		this.scoring += update;
		this.winger += update;
		this.goalkeeping += update;
		this.passing += update;
		this.defending += update;
		this.setPieces += update;
	}
	
	@Transient
	public Integer getAverage(){
		return ((this.stamina == null ? 0 : this.stamina.intValue()) +
				(this.playmaking == null ? 0 : this.playmaking.intValue())	+
				(this.scoring == null ? 0 : this.scoring.intValue()) +
				(this.winger == null ? 0 : this.winger.intValue()) +
				(this.goalkeeping == null ? 0 : this.goalkeeping.intValue()) +
				(this.passing == null ? 0 : this.passing.intValue()) +
				(this.defending == null ? 0 : this.defending.intValue()) +
				(this.setPieces == null ? 0 : this.setPieces.intValue()) +
				(this.condition == null ? 0 : this.condition.intValue())) / 9;
	}
	
	@Transient
	public Integer getStars(){
		return getAverage() * 5 / 100;
	}
	
	@Transient
	public Integer getOffenceAverage(){
		return ((this.playmaking == null ? 0 : this.playmaking.intValue())	 +
				(this.winger == null ? 0 : this.winger.intValue()) +
				(this.passing == null ? 0 : this.passing.intValue()) +
				(this.condition == null ? 0 : this.condition.intValue())) / 4;
	}
	
	@Transient
	public Integer getDefenceAverage(){
		return ((this.playmaking == null ? 0 : this.playmaking.intValue())	+
				(this.defending == null ? 0 : this.defending.intValue()) +
				(this.condition == null ? 0 : this.condition.intValue())) / 3;
	}
	
	@Transient
	public Integer getScoringAverage(){
		return ((this.scoring == null ? 0 : this.scoring.intValue()) +
				(this.condition == null ? 0 : this.condition.intValue())) / 2;
	}
	
	@Transient
	public Integer getGoalkeepingAverage(){
		return ((this.goalkeeping == null ? 0 : this.goalkeeping.intValue()) +
				(this.condition == null ? 0 : this.condition.intValue())) / 2;
	}
	
	@Transient
	public Integer getPiecesAverage(){
		return ((this.setPieces == null ? 0 : this.setPieces.intValue()) +
				(this.condition == null ? 0 : this.condition.intValue())) / 2;
	}
	
}
