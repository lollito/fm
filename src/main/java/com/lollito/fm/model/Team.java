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
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Team implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@Builder.Default
	@ToString.Exclude
	private List<Player> players = new ArrayList<>();
	
	@OneToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
	@JoinColumn( name = "formation_id" )
	@ToString.Exclude
	private Formation formation;

	@OneToOne(mappedBy = "team", cascade = CascadeType.ALL)
	@ToString.Exclude
	private TrainingPlan currentTrainingPlan;

	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
	@Builder.Default
	@ToString.Exclude
	private List<TrainingSession> trainingHistory = new ArrayList<>();
	
	@OneToOne(mappedBy = "team", fetch = FetchType.LAZY)
	@ToString.Exclude
	@JsonIgnore
	private Club club;

	@OneToOne(mappedBy = "under18", fetch = FetchType.LAZY)
	@ToString.Exclude
	@JsonIgnore
	private Club youthClub;

	@Transient
	public Long getOwnerClubId() {
		if (club != null) return club.getId();
		if (youthClub != null) return youthClub.getId();
		return null;
	}

	@Transient
	public void addPlayer(Player player){
		player.setTeam(this);
		this.players.add(player);
	}

	@Transient
	public void removePlayer(Player player){
		this.players.remove(player);
	}
	
	@Transient
	public Integer getAverage(){
		if (players == null || players.isEmpty()) return 0;
		int tot = 0;
		int count = 0;
		for (Player player : players) {
			if (player != null) {
				tot += player.getAverage();
				count++;
			}
		}
		return count > 0 ? tot/count : 0;
	}
	
	@Transient
	public Integer getOffenceAverage(){
		if (players == null || players.isEmpty()) return 0;
		int tot = 0;
		int count = 0;
		for (Player player : players) {
			if (player != null) {
				tot += player.getOffenceAverage();
				count++;
			}
		}
		return count > 0 ? tot/count : 0;
	}
	
	@Transient
	public Integer getDefenceAverage(){
		if (players == null || players.isEmpty()) return 0;
		int tot = 0;
		int count = 0;
		for (Player player : players) {
			if (player != null) {
				tot += player.getDefenceAverage();
				count++;
			}
		}
		return count > 0 ? tot/count : 0;
	}
	
}
