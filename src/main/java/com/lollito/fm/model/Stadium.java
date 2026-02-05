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
    @Builder.Default
    private Integer capacity = 5000;
    @Builder.Default
    private Integer baseCapacity = 5000; // Original capacity

    // Stadium capacity distribution
    @Builder.Default
    private Integer grandstandNord = 1250;
    @Builder.Default
    private Integer grandstandSud = 1250;
    @Builder.Default
    private Integer grandstandWest = 1250;
    @Builder.Default
    private Integer grandstandEst = 1250;

    // Stadium quality levels (1-10)
    @Builder.Default
    private Integer pitchQuality = 5;
    @Builder.Default
    private Integer facilitiesQuality = 3;
    @Builder.Default
    private Integer securityLevel = 3;
    @Builder.Default
    private Integer accessibilityLevel = 3;

    // Stadium features
    @Builder.Default
    private Boolean hasRoof = false;
    @Builder.Default
    private Boolean hasUndersoilHeating = false;
    @Builder.Default
    private Boolean hasVipBoxes = false;
    @Builder.Default
    private Boolean hasMediaCenter = false;
    @Builder.Default
    private Boolean hasMuseum = false;
    @Builder.Default
    private Boolean hasMegastore = false;

    // Financial aspects
    @Builder.Default
    private BigDecimal constructionCost = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal maintenanceCost = BigDecimal.valueOf(1000); // Monthly
    @Builder.Default
    private BigDecimal upgradeValue = BigDecimal.ZERO;
    
    // Stadium atmosphere and revenue multipliers
    @Builder.Default
    private Double atmosphereMultiplier = 1.0; // Affects player performance
    @Builder.Default
    private Double revenueMultiplier = 1.0; // Affects matchday revenue

    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
    
    public Stadium(String name){
        this();
    	this.name = name;
    }
}
