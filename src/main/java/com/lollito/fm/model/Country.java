package com.lollito.fm.model;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "country")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Country implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
    private String name;
    
    @jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	@Builder.Default
	private Boolean createLeague = Boolean.FALSE;
    
    private String flagUrl;
    
    public Country(String name, Boolean createLeague) {
		this();
		this.name = name;
		this.createLeague = createLeague;
	}
    
    public Country(String name, Boolean createLeague, String flagUrl) {
	this();
    	this.name = name;
    	this.createLeague = createLeague;
    	this.flagUrl = flagUrl;
    }

}
