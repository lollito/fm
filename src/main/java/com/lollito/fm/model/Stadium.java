package com.lollito.fm.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "stadium")
public class Stadium implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
    private String name;
    
    private Integer grandstandNord = 0;
    private Integer grandstandSud = 0;
    private Integer grandstandWest = 1;
    private Integer grandstandEst = 0;
    private Integer grandstandNordWest = 0;
    private Integer grandstandNordEst = 0;
    private Integer grandstandSudWest = 0;
    private Integer grandstandSudEst = 0;
  
    private Integer ground = 1;
    
    public Stadium(){
    	
    }
    
    public Stadium(String name){
    	this.name = name;
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
	
	public Integer getGrandstandNord() {
		return grandstandNord;
	}

	public void setGrandstandNord(Integer grandstandNord) {
		this.grandstandNord = grandstandNord;
	}

	public Integer getGrandstandSud() {
		return grandstandSud;
	}

	public void setGrandstandSud(Integer grandstandSud) {
		this.grandstandSud = grandstandSud;
	}

	public Integer getGrandstandWest() {
		return grandstandWest;
	}

	public void setGrandstandWest(Integer grandstandWest) {
		this.grandstandWest = grandstandWest;
	}

	public Integer getGrandstandEst() {
		return grandstandEst;
	}

	public void setGrandstandEst(Integer grandstandEst) {
		this.grandstandEst = grandstandEst;
	}

	public Integer getGrandstandNordWest() {
		return grandstandNordWest;
	}

	public void setGrandstandNordWest(Integer grandstandNordWest) {
		this.grandstandNordWest = grandstandNordWest;
	}

	public Integer getGrandstandNordEst() {
		return grandstandNordEst;
	}

	public void setGrandstandNordEst(Integer grandstandNordEst) {
		this.grandstandNordEst = grandstandNordEst;
	}

	public Integer getGrandstandSudWest() {
		return grandstandSudWest;
	}

	public void setGrandstandSudWest(Integer grandstandSudWest) {
		this.grandstandSudWest = grandstandSudWest;
	}

	public Integer getGrandstandSudEst() {
		return grandstandSudEst;
	}

	public void setGrandstandSudEst(Integer grandstandSudEst) {
		this.grandstandSudEst = grandstandSudEst;
	}

	public Integer getGround() {
		return ground;
	}

	public void setGround(Integer ground) {
		this.ground = ground;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Stadium)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Stadium other = (Stadium) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
}
