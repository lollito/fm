package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class Stadium implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
    @OneToOne(mappedBy = "stadium")
    @JsonIgnore
    @ToString.Exclude
    private Club club;

    private String name;
    private Integer capacity;
    private Integer baseCapacity; // Original capacity

    // Stadium quality levels (1-10)
    private Integer pitchQuality;
    private Integer facilitiesQuality;
    private Integer securityLevel;
    private Integer accessibilityLevel;

    // Stadium features
    private Boolean hasRoof;
    private Boolean hasUndersoilHeating;
    private Boolean hasVipBoxes;
    private Boolean hasMediaCenter;
    private Boolean hasMuseum;
    private Boolean hasMegastore;

    // Financial aspects
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost; // Monthly
    private BigDecimal upgradeValue; // Total invested in upgrades
    
    // Stadium atmosphere and revenue multipliers
    private Double atmosphereMultiplier; // Affects player performance
    private Double revenueMultiplier; // Affects matchday revenue

    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
    
    public Stadium(String name){
        this();
    	this.name = name;
        // Defaults for new stadium
        this.capacity = 5000;
        this.baseCapacity = 5000;
        this.pitchQuality = 5;
        this.facilitiesQuality = 3;
        this.securityLevel = 3;
        this.accessibilityLevel = 3;
        this.hasRoof = false;
        this.hasUndersoilHeating = false;
        this.hasVipBoxes = false;
        this.hasMediaCenter = false;
        this.hasMuseum = false;
        this.hasMegastore = false;
        this.constructionCost = BigDecimal.ZERO;
        this.maintenanceCost = BigDecimal.valueOf(1000);
        this.upgradeValue = BigDecimal.ZERO;
        this.atmosphereMultiplier = 1.0;
        this.revenueMultiplier = 1.0;
    }
}
