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
@Table(name = "maintenance_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class MaintenanceRecord implements Serializable {

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
    private MaintenanceType maintenanceType;

    private String description;
    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    private LocalDate scheduledDate;
    private LocalDate completedDate;

    // Maintenance effects
    private Integer qualityRestored;
    private String issuesFound;
    private String workPerformed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private Club club;

    private String contractorName;
    private Boolean isEmergencyMaintenance;

    public Boolean getIsEmergencyMaintenance() {
        return isEmergencyMaintenance;
    }
}
