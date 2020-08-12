package com.lollito.fm.model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "game")
public class Game implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
    private String name;
    
    @Column(name="crnt_date")
    private LocalDateTime currentDate;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@JoinColumn( name = "league_id" )
    private List<League> leagues = new ArrayList<>();
    
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

	public LocalDateTime getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(LocalDateTime currentDate) {
		this.currentDate = currentDate;
	}
	
	public void addDay(){
		this.currentDate = currentDate.plusDays(1);
	}
	
	public List<League> getLeagues() {
		return leagues;
	}

	public void setLeagues(List<League> leagues) {
		this.leagues = leagues;
	}

	public void addLeague(League league) {
		this.leagues.add(league);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Game)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Game other = (Game) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
}
