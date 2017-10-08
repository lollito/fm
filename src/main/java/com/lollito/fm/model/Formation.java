package com.lollito.fm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "formation")
public class Formation implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "module_id" )
	private Module module;
	
	@Column(name="have_ball")
	@Type(type = "yes_no")
	private Boolean haveBall = Boolean.FALSE;
	
//	private Player goalKeeper;
//	
//	
//	private List<Player> centralDefenders = new ArrayList<>();
//	private List<Player> wingBacks = new ArrayList<>();
//	private List<Player> midfielders = new ArrayList<>();
//	private List<Player> wings = new ArrayList<>();
//	private List<Player> forwards = new ArrayList<>();
	
	@ManyToMany
    @JoinTable(name = "formation_player", joinColumns = @JoinColumn(name = "formation_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
	private List<Player> players = new ArrayList<>();
	
	@ManyToMany
    @JoinTable(name = "formation_substitutes", joinColumns = @JoinColumn(name = "formation_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
	private List<Player> substitutes = new ArrayList<>();
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Module getModule() {
		return module;
	}
	
	public void setModule(Module module) {
		this.module = module;
	}
	
	public void setHaveBall(Boolean haveBall) {
		this.haveBall = haveBall;
	}
	
	public Boolean getHaveBall() {
		return haveBall;
	}
	
	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public void addPlayer(Player player) {
		this.players.add(player);
	}
	
	public List<Player> getSubstitutes() {
		return substitutes;
	}

	public void setSubstitutes(List<Player> substitutes) {
		this.substitutes = substitutes;
	}

	@Transient
	public Player getGoalKeeper() {
		for (Player player : players) {
			if(player.getRole().getvalue() == PlayerRole.GOALKEEPER.getvalue()){
				return player;
			}
		}
		return null;
	}
	
	@Transient
	public List<Player> getCentralDefenders() {
		return getPlayersByRole(PlayerRole.CENTRALDEFENDER);
	}
	
	@Transient
	public List<Player> getWingBacks() {
		return getPlayersByRole(PlayerRole.WINGBACK);
	}
	
	@Transient
	public List<Player> getMidfielders() {
		return getPlayersByRole(PlayerRole.MIDFIELDER);
	}
	
	@Transient
	public List<Player> getWings() {
		return getPlayersByRole(PlayerRole.WING);
	}
	
	@Transient
	public List<Player> getForwards() {
		return getPlayersByRole(PlayerRole.FORWARD);
	}
	
	@Transient
	private List<Player> getPlayersByRole(PlayerRole role){
		List<Player> ret = new ArrayList<>();
		for (Player player : players) {
			if(player.getRole().getvalue() == role.getvalue()){
				ret.add(player);
			}
		}
		return ret;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Formation)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Formation other = (Formation) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("players", players)
				.append("module", module)
				.toString();
	}
}
