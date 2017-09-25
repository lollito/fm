package com.lollito.fm.model;
import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "club")
public class Club implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
    private String name;
    
    private LocalDate foundation;
    
    @OneToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "team_id" )
    private Team team;
    
    @OneToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "under18_id" )
    private Team under18;
    
    @OneToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "stadium_id" )
    private Stadium stadium;
    
    @ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "game_id" )
    private Game game;
    
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
	
	public LocalDate getFoundation() {
		return foundation;
	}
	
	public void setFoundation(LocalDate foundation) {
		this.foundation = foundation;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public void setTeam(Team team) {
		this.team = team;
	}
	
	public Team getUnder18() {
		return under18;
	}

	public void setUnder18(Team under18) {
		this.under18 = under18;
	}

	public Stadium getStadium() {
		return stadium;
	}
	
	public void setStadium(Stadium stadium) {
		this.stadium = stadium;
	}
    
	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(name).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Club)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Club other = (Club) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
}
