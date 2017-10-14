package com.lollito.fm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
public class Player implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	private String surname;
	private Date birth;
	
//	Decide quanta abilità perde un giocatore durante il corso di una partita, a causa della fatica.
	private Integer stamina;
	
//	L'abilità di controllare il gioco e trasformarlo in occasioni da goal. 
	private Integer playmaking;
	
// 	L'abilità di mettere la palla in rete.
	private Integer scoring;
	
// 	L'abilità di finalizzare azioni da goal grazie alle discese sulle fasce. 
	private Integer winger;
	
// 	Evitare che la palla entri nella propria rete. 
	private Integer goalkeeping;
	
// 	Giocatori che sanno come fare il passaggio decisivo sono di grande aiuto per l'attacco della squadra. 
	private Integer passing;
	
// 	L'abilità di fermare gli attacchi avversari. 
	private Integer defending;
	
// 	Il risultato delle punizioni e dei rigori dipende da quanto è abile il tuo specialista in calci piazzati. 
	private Integer setPieces;

	@Column(name = "cndtion")
	private Double condition = 100.0;
	
	@Enumerated(EnumType.ORDINAL)
	private PlayerRole role;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "team_id" )
	@JsonIgnore
    private Team team;
	
	@ManyToMany(mappedBy = "players", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<Formation> formations = new ArrayList<>();
	
	public Player() {
		
	}
	
	public Player(String name, String surname, Date birth) {
		//this.id = ThreadLocalRandom.current().nextLong(1, 2000000000);
		this.name = name;
		this.surname = surname;
		this.birth = birth;
	}

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
	
	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public Integer getStamina() {
		return stamina;
	}

	public void setStamina(Integer stamina) {
		this.stamina = stamina;
	}

	public Integer getPlaymaking() {
		return playmaking;
	}

	public void setPlaymaking(Integer playmaking) {
		this.playmaking = playmaking;
	}

	public Integer getScoring() {
		return scoring;
	}

	public void setScoring(Integer scoring) {
		this.scoring = scoring;
	}

	public Integer getWinger() {
		return winger;
	}

	public void setWinger(Integer winger) {
		this.winger = winger;
	}

	public Integer getGoalkeeping() {
		return goalkeeping;
	}

	public void setGoalkeeping(Integer goalkeeping) {
		this.goalkeeping = goalkeeping;
	}

	public Integer getPassing() {
		return passing;
	}

	public void setPassing(Integer passing) {
		this.passing = passing;
	}

	public Integer getDefending() {
		return defending;
	}

	public void setDefending(Integer defending) {
		this.defending = defending;
	}

	public Integer getSetPieces() {
		return setPieces;
	}

	public void setSetPieces(Integer setPieces) {
		this.setPieces = setPieces;
	}

	public Double getCondition() {
		return condition;
	}

	public void setCondition(Double condition) {
		this.condition = condition;
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
	
	public PlayerRole getRole() {
		return role;
	}

	public void setRole(PlayerRole role) {
		this.role = role;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	@Transient
	public Integer getAverage(){
		return (this.stamina + 
				this.playmaking	+ 
				this.scoring + 
				this.winger +
				this.goalkeeping +
				this.passing +
				this.defending +
				this.setPieces +
				this.condition.intValue()) / 9;
	}
	
	@Transient
	public Integer getOffenceAverage(){
		return (this.playmaking	+ 
				this.scoring + 
				this.winger +
				this.passing +
				this.setPieces +
				this.condition.intValue()) / 6;
	}
	
	@Transient
	public Integer getDefenceAverage(){
		return (this.playmaking	+ 
				this.goalkeeping +
				this.defending +
				this.setPieces +
				this.condition.intValue()) / 5;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Player)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Player other = (Player) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("id", id)
				.append("name", name)
				.append("surname", surname)
				.append("birth", birth)
				.append("stamina", stamina)
				.append("playmaking", playmaking)
				.append("scoring", scoring)
				.append("winger", winger)
				.append("goalkeeping", goalkeeping)
				.append("passing", passing)
				.append("defending", defending)
				.append("setPieces", setPieces)
				.append("condition", condition)
				.toString();
	}
	
}
