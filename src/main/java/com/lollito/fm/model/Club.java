package com.lollito.fm.model;
import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.CascadeType;
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
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "club")
public class Club implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
    private String name;
    
    private LocalDate foundation;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "team_id" )
    @JsonIgnore
    private Team team;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "under18_id" )
    @JsonIgnore
    private Team under18;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "stadium_id" )
    @JsonIgnore
    private Stadium stadium;
    
    @ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "game_id" )
    @JsonIgnore
    private Game game;
    
    @ManyToOne( fetch = FetchType.LAZY  )
   	@JoinColumn( name = "user_id" )
    @JsonIgnore
    private User user;
    
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
