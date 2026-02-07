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
@Table(name = "training_facility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class TrainingFacility implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(mappedBy = "trainingFacility")
    @JsonIgnore
    @ToString.Exclude
    private Club club;

    private String name;

    // Facility quality levels (1-10)
    private Integer overallQuality;
    private Integer pitchQuality;
    private Integer gymQuality;
    private Integer medicalFacilityQuality;
    private Integer analysisRoomQuality;

    // Training bonuses (percentage improvements)
    private Double physicalTrainingBonus;
    private Double technicalTrainingBonus;
    private Double tacticalTrainingBonus;
    private Double mentalTrainingBonus;

    // Facility features
    private Integer numberOfPitches;
    private Boolean hasIndoorFacilities;
    private Boolean hasHydrotherapy;
    private Boolean hasAltitudeTraining;
    private Boolean hasVideoAnalysis;
    private Boolean hasNutritionCenter;

    // Financial aspects
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost; // Monthly
    private BigDecimal upgradeValue;

    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;

    public Boolean getHasIndoorFacilities() {
        return hasIndoorFacilities;
    }

    public Boolean getHasHydrotherapy() {
        return hasHydrotherapy;
    }

    public Boolean getHasAltitudeTraining() {
        return hasAltitudeTraining;
    }

    public Boolean getHasVideoAnalysis() {
        return hasVideoAnalysis;
    }

    public Boolean getHasNutritionCenter() {
        return hasNutritionCenter;
    }
}
