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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "formation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Formation implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "module_id" )
	@ToString.Exclude
	private Module module;
	
	@Column(name="have_ball")
	@jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	@Builder.Default
	private Boolean haveBall = Boolean.FALSE;
	
	@OneToOne( mappedBy= "formation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
	@JsonIgnore
	@ToString.Exclude
	private Team team;
	
	@ManyToMany
    @JoinTable(name = "formation_player", joinColumns = @JoinColumn(name = "formation_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
	@OrderColumn(name = "idx")
	@Builder.Default
	@ToString.Exclude
	private List<Player> players = new ArrayList<>();
	
	@ManyToMany
    @JoinTable(name = "formation_substitutes", joinColumns = @JoinColumn(name = "formation_id"), inverseJoinColumns = @JoinColumn(name = "player_id"))
	@Builder.Default
	@ToString.Exclude
	private List<Player> substitutes = new ArrayList<>();
	
	@Enumerated(EnumType.ORDINAL)
	@Builder.Default
	private Mentality mentality = Mentality.NORMAL;
	
	public void addPlayer(Player player) {
		this.players.add(player);
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
	
}
