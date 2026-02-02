package com.lollito.fm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "team")
public class Team implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	private List<Player> players = new ArrayList<>();
	
	@OneToOne( fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
	@JoinColumn( name = "formation_id" )
	private Formation formation;
	
	public Team(){
		
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Player> getPlayers() {
		return players;
	}
	
	public void setPlayers(List<Player> players) {
		this.players = players;
	}
	
	@Transient
	public void addPlayer(Player player){
		player.setTeam(this);
		this.players.add(player);
	}
	
	
	public Formation getFormation() {
		return formation;
	}

	public void setFormation(Formation formation) {
		this.formation = formation;
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
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Team)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Team other = (Team) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("id", id)
				//.append("players", players)
				.toString();
	}
	
}
