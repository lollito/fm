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

@Entity
@Table(name = "team")
public class Team implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	private List<Player> players = new ArrayList<>();
	
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
	
	public void addPlayer(Player player){
		player.setTeam(this);
		this.players.add(player);
	}
	
	@Transient
	public Integer getAverage(){
		int tot = 0;
		for (Player player : players) {
			tot += player.getAverage();
		}
		return tot/players.size();
	}
	
	@Transient
	public Integer getOffenceAverage(){
		int tot = 0;
		for (Player player : players) {
			tot += player.getOffenceAverage();
		}
		return tot/players.size();
	}
	
	@Transient
	public Integer getDefenceAverage(){
		int tot = 0;
		for (Player player : players) {
			tot += player.getDefenceAverage();
		}
		return tot/players.size();
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
