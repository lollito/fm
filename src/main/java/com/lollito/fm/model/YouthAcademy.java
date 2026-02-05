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
@Table(name = "youth_academy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class YouthAcademy implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(mappedBy = "youthAcademy")
    @JsonIgnore
    @ToString.Exclude
    private Club club;

    private String name;

    // Academy quality levels (1-10)
    private Integer overallQuality;
    private Integer coachingQuality;
    private Integer facilitiesQuality;
    private Integer educationQuality;
    private Integer scoutingNetwork;

    // Youth development bonuses
    private Double talentGenerationBonus; // Better youth players generated
    private Double developmentSpeedBonus; // Faster player development
    private Double retentionBonus; // Players more likely to stay

    // Academy features
    private Integer numberOfPitches;
    private Boolean hasEducationCenter;
    private Boolean hasResidentialFacilities;
    private Boolean hasScoutingNetwork;
    private Boolean hasPartnershipPrograms;

    // Academy capacity and staffing
    private Integer maxYouthPlayers;
    private Integer numberOfCoaches;
    private Integer numberOfScouts;
    private Integer numberOfEducators;

    // Financial aspects
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost; // Monthly
    private BigDecimal upgradeValue;

    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;

    public Boolean getHasEducationCenter() {
        return hasEducationCenter;
    }

    public Boolean getHasResidentialFacilities() {
        return hasResidentialFacilities;
    }

    public Boolean getHasScoutingNetwork() {
        return hasScoutingNetwork;
    }

    public Boolean getHasPartnershipPrograms() {
        return hasPartnershipPrograms;
    }
}
