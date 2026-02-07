package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
@Table(name = "medical_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class MedicalCenter implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(mappedBy = "medicalCenter")
    @JsonIgnore
    @ToString.Exclude
    private Club club;

    private String name;

    // Medical facility quality levels (1-10)
    private Integer overallQuality;
    private Integer diagnosticEquipment;
    private Integer rehabilitationFacilities;
    private Integer surgicalCapabilities;
    private Integer preventiveCareLevel;

    // Medical bonuses
    private Double injuryPreventionBonus; // Reduces injury probability
    private Double recoverySpeedBonus; // Faster injury recovery
    private Double fitnessMaintenanceBonus; // Better fitness retention

    // Medical center features
    private Boolean hasMriScanner;
    private Boolean hasHyperbaricChamber;
    private Boolean hasCryotherapy;
    private Boolean hasPhysiotherapy;
    private Boolean hasSportsScience;

    // Staffing
    private Integer numberOfDoctors;
    private Integer numberOfPhysiotherapists;
    private Integer numberOfSportsScientists;

    // Financial aspects
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost; // Monthly
    private BigDecimal upgradeValue;

    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;

    public Boolean getHasMriScanner() {
        return hasMriScanner;
    }

    public Boolean getHasHyperbaricChamber() {
        return hasHyperbaricChamber;
    }

    public Boolean getHasCryotherapy() {
        return hasCryotherapy;
    }

    public Boolean getHasPhysiotherapy() {
        return hasPhysiotherapy;
    }

    public Boolean getHasSportsScience() {
        return hasSportsScience;
    }
}
