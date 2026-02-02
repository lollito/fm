package com.lollito.fm.model;

import java.io.Serializable;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "formation")
public class Formation implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "module_id" )
	private Module module;
	
	@Column(name="have_ball")
	@jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	private Boolean haveBall = Boolean.FALSE;
	
//	@OneToOne( mappedBy= "homeFormation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
//	private SimulationMatch simulationMatch;
//	
//	@OneToOne( mappedBy= "awayFormation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
//	private SimulationMatch simulationMatchAway;
	
	@OneToOne( mappedBy= "formation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
	private Team team;
	
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
	@OrderColumn(name = "idx")
	private List<Player> players = new ArrayList<>();
	
	@ManyToMany
    @JoinTable(name = "formation_substitutes", joinColumns = @JoinColumn(name = "formation_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
	private List<Player> substitutes = new ArrayList<>();
	
	@Enumerated(EnumType.ORDINAL)
	private Mentality mentality = Mentality.NORMAL;
	
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

	public Mentality getMentality() {
		return mentality;
	}

	public void setMentality(Mentality mentality) {
		this.mentality = mentality;
	}

	public Formation copy() {
		Formation copy = new Formation();
		copy.setModule(this.module);
		copy.setMentality(this.mentality);
		copy.setPlayers(new ArrayList<>(this.players));
		copy.setSubstitutes(new ArrayList<>(this.substitutes));
		return copy;
	}

	@Transient
	public Player getGoalKeeper() {
		if (players == null || players.isEmpty()) return null;
		return players.get(0);
	}

	@Transient
	public List<Player> getCentralDefenders() {
		if (module == null || players == null) return new ArrayList<>();
		int start = 1;
		int end = Math.min(players.size(), start + module.getCd());
		return getSafeSubList(start, end);
	}

	@Transient
	public List<Player> getWingBacks() {
		if (module == null || players == null) return new ArrayList<>();
		int start = 1 + module.getCd();
		int end = Math.min(players.size(), start + module.getWb());
		return getSafeSubList(start, end);
	}

	@Transient
	public List<Player> getMidfielders() {
		if (module == null || players == null) return new ArrayList<>();
		int start = 1 + module.getCd() + module.getWb();
		int end = Math.min(players.size(), start + module.getMf());
		return getSafeSubList(start, end);
	}

	@Transient
	public List<Player> getWings() {
		if (module == null || players == null) return new ArrayList<>();
		int start = 1 + module.getCd() + module.getWb() + module.getMf();
		int end = Math.min(players.size(), start + module.getWng());
		return getSafeSubList(start, end);
	}

	@Transient
	public List<Player> getForwards() {
		if (module == null || players == null) return new ArrayList<>();
		int start = 1 + module.getCd() + module.getWb() + module.getMf() + module.getWng();
		int end = Math.min(players.size(), start + module.getFw());
		return getSafeSubList(start, end);
	}

	@Transient
	private List<Player> getSafeSubList(int start, int end) {
		if (start >= players.size() || start >= end) return new ArrayList<>();
		return new ArrayList<>(players.subList(start, end));
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
