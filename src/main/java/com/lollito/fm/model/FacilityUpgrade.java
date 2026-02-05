package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "facility_upgrade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class FacilityUpgrade implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    private FacilityType facilityType;

    private Long facilityId; // Generic reference to facility

    @Enumerated(EnumType.STRING)
    private UpgradeType upgradeType;

    private String upgradeName;
    private String description;

    // Upgrade specifications
    private BigDecimal cost;
    private Integer durationDays;
    private Integer qualityImprovement;

    // Upgrade effects
    private String bonusEffects; // JSON string of bonus effects
    private Double maintenanceCostIncrease; // Percentage increase

    @Enumerated(EnumType.STRING)
    private UpgradeStatus status;

    private LocalDate startDate;
    private LocalDate completionDate;
    private LocalDate plannedCompletionDate;

    // Prerequisites
    private Integer requiredQualityLevel;
    private String requiredUpgrades; // JSON array of required upgrade IDs

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private Club club;

    private String contractorName;
    private String notes;
}
