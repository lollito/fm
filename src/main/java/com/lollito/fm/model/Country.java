package com.lollito.fm.model;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "country")
public class Country implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
    private String name;
    
    @jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	private Boolean createLeague = Boolean.FALSE;
    
    private String flagUrl;
    
    public Country() {
    	
	}
    
    public Country(String name, Boolean createLeague) {
		this.name = name;
		this.createLeague = createLeague;
	}
    
    public Country(String name, Boolean createLeague, String flagUrl) {
    	this.name = name;
    	this.createLeague = createLeague;
    	this.flagUrl = flagUrl;
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
	
	public Boolean getCreateLeague() {
		return createLeague;
	}

	public void setCreateLeague(Boolean createLeague) {
		this.createLeague = createLeague;
	}

	public String getFlagUrl() {
		return flagUrl;
	}

	public void setFlagUrl(String flagUrl) {
		this.flagUrl = flagUrl;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(name).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Country)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			Country other = (Country) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
}
