package com.lollito.fm.model;

import java.io.Serializable;

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
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "stadium")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Stadium implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
    private String name;
    
	@Builder.Default
    private Integer grandstandNord = 0;
	@Builder.Default
    private Integer grandstandSud = 0;
	@Builder.Default
    private Integer grandstandWest = 1;
	@Builder.Default
    private Integer grandstandEst = 0;
	@Builder.Default
    private Integer grandstandNordWest = 0;
	@Builder.Default
    private Integer grandstandNordEst = 0;
	@Builder.Default
    private Integer grandstandSudWest = 0;
	@Builder.Default
    private Integer grandstandSudEst = 0;
  
	@Builder.Default
    private Integer ground = 1;
    
    public Stadium(String name){
	this();
    	this.name = name;
    }

    @Transient
    public Integer getCapacity() {
        return grandstandNord + grandstandSud + grandstandWest + grandstandEst +
               grandstandNordWest + grandstandNordEst + grandstandSudWest + grandstandSudEst;
    }

}
