# Infrastructure Management System Implementation

## Overview
Implement a comprehensive infrastructure management system allowing clubs to upgrade stadiums, training facilities, medical centers, and youth academies. Each facility provides specific bonuses and requires ongoing maintenance costs.

## Technical Requirements

### Database Schema Changes

#### Enhanced Stadium Entity
```java
@Entity
@Table(name = "stadium")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "stadium")
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
    
    @OneToMany(mappedBy = "stadium", cascade = CascadeType.ALL)
    private List<StadiumUpgrade> upgrades = new ArrayList<>();
    
    @OneToMany(mappedBy = "stadium", cascade = CascadeType.ALL)
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();
    
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
```

#### New Entity: TrainingFacility
```java
@Entity
@Table(name = "training_facility")
public class TrainingFacility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "trainingFacility")
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
    
    @OneToMany(mappedBy = "trainingFacility", cascade = CascadeType.ALL)
    private List<FacilityUpgrade> upgrades = new ArrayList<>();
    
    @OneToMany(mappedBy = "trainingFacility", cascade = CascadeType.ALL)
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();
    
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
```

#### New Entity: MedicalCenter
```java
@Entity
@Table(name = "medical_center")
public class MedicalCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "medicalCenter")
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
    
    @OneToMany(mappedBy = "medicalCenter", cascade = CascadeType.ALL)
    private List<FacilityUpgrade> upgrades = new ArrayList<>();
    
    @OneToMany(mappedBy = "medicalCenter", cascade = CascadeType.ALL)
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();
    
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
```

#### New Entity: YouthAcademy
```java
@Entity
@Table(name = "youth_academy")
public class YouthAcademy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "youthAcademy")
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
    
    @OneToMany(mappedBy = "youthAcademy", cascade = CascadeType.ALL)
    private List<FacilityUpgrade> upgrades = new ArrayList<>();
    
    @OneToMany(mappedBy = "youthAcademy", cascade = CascadeType.ALL)
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();
    
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
```

#### New Entity: FacilityUpgrade
```java
@Entity
@Table(name = "facility_upgrade")
public class FacilityUpgrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Club club;
    
    private String contractorName;
    private String notes;
}
```

#### New Entity: MaintenanceRecord
```java
@Entity
@Table(name = "maintenance_record")
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Club club;
    
    private String contractorName;
    private Boolean isEmergencyMaintenance;
}
```

#### Enums to Create
```java
public enum FacilityType {
    STADIUM("Stadium"),
    TRAINING_FACILITY("Training Facility"),
    MEDICAL_CENTER("Medical Center"),
    YOUTH_ACADEMY("Youth Academy");
    
    private final String displayName;
}

public enum UpgradeType {
    CAPACITY_EXPANSION("Capacity Expansion"),
    QUALITY_IMPROVEMENT("Quality Improvement"),
    FEATURE_ADDITION("Feature Addition"),
    TECHNOLOGY_UPGRADE("Technology Upgrade"),
    SAFETY_UPGRADE("Safety Upgrade");
    
    private final String displayName;
}

public enum UpgradeStatus {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    DELAYED("Delayed");
    
    private final String displayName;
}

public enum MaintenanceType {
    ROUTINE("Routine Maintenance"),
    PREVENTIVE("Preventive Maintenance"),
    CORRECTIVE("Corrective Maintenance"),
    EMERGENCY("Emergency Maintenance"),
    SEASONAL("Seasonal Maintenance");
    
    private final String displayName;
}

public enum MaintenanceStatus {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
}
```

### Service Layer Implementation

#### InfrastructureService
```java
@Service
public class InfrastructureService {
    
    @Autowired
    private StadiumRepository stadiumRepository;
    
    @Autowired
    private TrainingFacilityRepository trainingFacilityRepository;
    
    @Autowired
    private MedicalCenterRepository medicalCenterRepository;
    
    @Autowired
    private YouthAcademyRepository youthAcademyRepository;
    
    @Autowired
    private FacilityUpgradeRepository facilityUpgradeRepository;
    
    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;
    
    @Autowired
    private FinancialService financialService;
    
    /**
     * Get infrastructure overview for a club
     */
    public InfrastructureOverviewDTO getInfrastructureOverview(Long clubId) {
        Club club = clubService.findById(clubId);
        
        Stadium stadium = club.getStadium();
        TrainingFacility trainingFacility = club.getTrainingFacility();
        MedicalCenter medicalCenter = club.getMedicalCenter();
        YouthAcademy youthAcademy = club.getYouthAcademy();
        
        // Get ongoing upgrades
        List<FacilityUpgrade> ongoingUpgrades = facilityUpgradeRepository
            .findByClubAndStatusIn(club, Arrays.asList(UpgradeStatus.PLANNED, UpgradeStatus.IN_PROGRESS));
        
        // Get upcoming maintenance
        List<MaintenanceRecord> upcomingMaintenance = maintenanceRecordRepository
            .findByClubAndStatusAndScheduledDateAfter(club, MaintenanceStatus.SCHEDULED, LocalDate.now());
        
        // Calculate total monthly maintenance costs
        BigDecimal totalMaintenanceCost = calculateTotalMaintenanceCost(club);
        
        return InfrastructureOverviewDTO.builder()
            .stadium(stadium != null ? convertToDTO(stadium) : null)
            .trainingFacility(trainingFacility != null ? convertToDTO(trainingFacility) : null)
            .medicalCenter(medicalCenter != null ? convertToDTO(medicalCenter) : null)
            .youthAcademy(youthAcademy != null ? convertToDTO(youthAcademy) : null)
            .ongoingUpgrades(ongoingUpgrades.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .upcomingMaintenance(upcomingMaintenance.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .totalMonthlyMaintenanceCost(totalMaintenanceCost)
            .totalInfrastructureValue(calculateTotalInfrastructureValue(club))
            .build();
    }
    
    /**
     * Get available upgrades for a facility
     */
    public List<UpgradeOptionDTO> getAvailableUpgrades(Long clubId, FacilityType facilityType) {
        Club club = clubService.findById(clubId);
        List<UpgradeOptionDTO> availableUpgrades = new ArrayList<>();
        
        switch (facilityType) {
            case STADIUM -> availableUpgrades = getStadiumUpgradeOptions(club.getStadium());
            case TRAINING_FACILITY -> availableUpgrades = getTrainingFacilityUpgradeOptions(club.getTrainingFacility());
            case MEDICAL_CENTER -> availableUpgrades = getMedicalCenterUpgradeOptions(club.getMedicalCenter());
            case YOUTH_ACADEMY -> availableUpgrades = getYouthAcademyUpgradeOptions(club.getYouthAcademy());
        }
        
        return availableUpgrades;
    }
    
    /**
     * Start facility upgrade
     */
    @Transactional
    public FacilityUpgrade startUpgrade(Long clubId, StartUpgradeRequest request) {
        Club club = clubService.findById(clubId);
        
        // Validate club has sufficient funds
        if (club.getFinance().getBalance().compareTo(request.getCost()) < 0) {
            throw new InsufficientFundsException("Club does not have sufficient funds for this upgrade");
        }
        
        // Validate prerequisites
        validateUpgradePrerequisites(club, request);
        
        // Create upgrade record
        FacilityUpgrade upgrade = FacilityUpgrade.builder()
            .facilityType(request.getFacilityType())
            .facilityId(request.getFacilityId())
            .upgradeType(request.getUpgradeType())
            .upgradeName(request.getUpgradeName())
            .description(request.getDescription())
            .cost(request.getCost())
            .durationDays(request.getDurationDays())
            .qualityImprovement(request.getQualityImprovement())
            .bonusEffects(request.getBonusEffects())
            .maintenanceCostIncrease(request.getMaintenanceCostIncrease())
            .status(UpgradeStatus.IN_PROGRESS)
            .startDate(LocalDate.now())
            .plannedCompletionDate(LocalDate.now().plusDays(request.getDurationDays()))
            .club(club)
            .contractorName(request.getContractorName())
            .build();
        
        upgrade = facilityUpgradeRepository.save(upgrade);
        
        // Process payment
        financialService.processTransaction(clubId, CreateTransactionRequest.builder()
            .type(TransactionType.EXPENSE)
            .category(TransactionCategory.FACILITY_MAINTENANCE)
            .amount(request.getCost())
            .description("Facility upgrade: " + request.getUpgradeName())
            .reference("UPGRADE_" + upgrade.getId())
            .effectiveDate(LocalDate.now())
            .build());
        
        return upgrade;
    }
    
    /**
     * Complete facility upgrade
     */
    @Transactional
    public void completeUpgrade(Long upgradeId) {
        FacilityUpgrade upgrade = facilityUpgradeRepository.findById(upgradeId)
            .orElseThrow(() -> new EntityNotFoundException("Upgrade not found"));
        
        if (upgrade.getStatus() != UpgradeStatus.IN_PROGRESS) {
            throw new IllegalStateException("Upgrade is not in progress");
        }
        
        // Update upgrade status
        upgrade.setStatus(UpgradeStatus.COMPLETED);
        upgrade.setCompletionDate(LocalDate.now());
        facilityUpgradeRepository.save(upgrade);
        
        // Apply upgrade effects to facility
        applyUpgradeEffects(upgrade);
        
        // Schedule next maintenance if needed
        scheduleNextMaintenance(upgrade.getFacilityType(), upgrade.getFacilityId());
    }
    
    /**
     * Apply upgrade effects to facility
     */
    private void applyUpgradeEffects(FacilityUpgrade upgrade) {
        switch (upgrade.getFacilityType()) {
            case STADIUM -> applyStadiumUpgrade(upgrade);
            case TRAINING_FACILITY -> applyTrainingFacilityUpgrade(upgrade);
            case MEDICAL_CENTER -> applyMedicalCenterUpgrade(upgrade);
            case YOUTH_ACADEMY -> applyYouthAcademyUpgrade(upgrade);
        }
    }
    
    /**
     * Apply stadium upgrade effects
     */
    private void applyStadiumUpgrade(FacilityUpgrade upgrade) {
        Stadium stadium = stadiumRepository.findById(upgrade.getFacilityId())
            .orElseThrow(() -> new EntityNotFoundException("Stadium not found"));
        
        switch (upgrade.getUpgradeType()) {
            case CAPACITY_EXPANSION -> {
                int capacityIncrease = Integer.parseInt(upgrade.getBonusEffects());
                stadium.setCapacity(stadium.getCapacity() + capacityIncrease);
            }
            case QUALITY_IMPROVEMENT -> {
                stadium.setPitchQuality(Math.min(stadium.getPitchQuality() + upgrade.getQualityImprovement(), 10));
                stadium.setFacilitiesQuality(Math.min(stadium.getFacilitiesQuality() + upgrade.getQualityImprovement(), 10));
            }
            case FEATURE_ADDITION -> {
                applyStadiumFeatureAddition(stadium, upgrade.getBonusEffects());
            }
        }
        
        // Update maintenance cost
        if (upgrade.getMaintenanceCostIncrease() != null) {
            BigDecimal currentCost = stadium.getMaintenanceCost();
            BigDecimal increase = currentCost.multiply(BigDecimal.valueOf(upgrade.getMaintenanceCostIncrease()));
            stadium.setMaintenanceCost(currentCost.add(increase));
        }
        
        // Update upgrade value
        stadium.setUpgradeValue(stadium.getUpgradeValue().add(upgrade.getCost()));
        stadium.setLastUpgradeDate(LocalDate.now());
        
        // Recalculate multipliers
        updateStadiumMultipliers(stadium);
        
        stadiumRepository.save(stadium);
    }
    
    /**
     * Process monthly maintenance for all facilities
     */
    @Scheduled(cron = "0 0 10 1 * *") // First day of month at 10 AM
    public void processMonthlyMaintenance() {
        List<Club> allClubs = clubService.findAll();
        
        for (Club club : allClubs) {
            processClubMaintenance(club);
        }
    }
    
    /**
     * Process maintenance for a specific club
     */
    private void processClubMaintenance(Club club) {
        BigDecimal totalMaintenanceCost = BigDecimal.ZERO;
        
        // Stadium maintenance
        if (club.getStadium() != null) {
            totalMaintenanceCost = totalMaintenanceCost.add(
                processStadiumMaintenance(club.getStadium()));
        }
        
        // Training facility maintenance
        if (club.getTrainingFacility() != null) {
            totalMaintenanceCost = totalMaintenanceCost.add(
                processTrainingFacilityMaintenance(club.getTrainingFacility()));
        }
        
        // Medical center maintenance
        if (club.getMedicalCenter() != null) {
            totalMaintenanceCost = totalMaintenanceCost.add(
                processMedicalCenterMaintenance(club.getMedicalCenter()));
        }
        
        // Youth academy maintenance
        if (club.getYouthAcademy() != null) {
            totalMaintenanceCost = totalMaintenanceCost.add(
                processYouthAcademyMaintenance(club.getYouthAcademy()));
        }
        
        // Process total maintenance payment
        if (totalMaintenanceCost.compareTo(BigDecimal.ZERO) > 0) {
            financialService.processTransaction(club.getId(), CreateTransactionRequest.builder()
                .type(TransactionType.EXPENSE)
                .category(TransactionCategory.FACILITY_MAINTENANCE)
                .amount(totalMaintenanceCost)
                .description("Monthly facility maintenance")
                .reference("MAINTENANCE_" + LocalDate.now().toString())
                .effectiveDate(LocalDate.now())
                .build());
        }
    }
    
    /**
     * Check for facility degradation and schedule maintenance
     */
    @Scheduled(cron = "0 0 6 * * *") // Daily at 6 AM
    public void checkFacilityCondition() {
        List<Club> allClubs = clubService.findAll();
        
        for (Club club : allClubs) {
            checkAndScheduleMaintenance(club);
        }
    }
    
    /**
     * Get facility upgrade options for stadium
     */
    private List<UpgradeOptionDTO> getStadiumUpgradeOptions(Stadium stadium) {
        List<UpgradeOptionDTO> options = new ArrayList<>();
        
        if (stadium == null) {
            // Options for building a new stadium
            options.add(createNewStadiumOption());
            return options;
        }
        
        // Capacity expansion options
        if (stadium.getCapacity() < 80000) {
            options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.CAPACITY_EXPANSION)
                .name("Capacity Expansion (+5,000 seats)")
                .description("Expand stadium capacity by 5,000 seats")
                .cost(BigDecimal.valueOf(2500000))
                .durationDays(180)
                .effects("Increased matchday revenue, better atmosphere")
                .requirements("Stadium quality level 5+")
                .build());
        }
        
        // Quality improvements
        if (stadium.getPitchQuality() < 10) {
            options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.QUALITY_IMPROVEMENT)
                .name("Pitch Quality Upgrade")
                .description("Improve pitch quality and playing conditions")
                .cost(BigDecimal.valueOf(500000))
                .durationDays(30)
                .effects("Better player performance, reduced injury risk")
                .requirements("None")
                .build());
        }
        
        // Feature additions
        if (!stadium.getHasUndersoilHeating()) {
            options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.FEATURE_ADDITION)
                .name("Undersoil Heating System")
                .description("Install undersoil heating for all-weather play")
                .cost(BigDecimal.valueOf(1000000))
                .durationDays(90)
                .effects("Matches can be played in all weather conditions")
                .requirements("Stadium quality level 6+")
                .build());
        }
        
        return options;
    }
    
    /**
     * Calculate total infrastructure value
     */
    private BigDecimal calculateTotalInfrastructureValue(Club club) {
        BigDecimal totalValue = BigDecimal.ZERO;
        
        if (club.getStadium() != null) {
            totalValue = totalValue.add(club.getStadium().getConstructionCost())
                                  .add(club.getStadium().getUpgradeValue());
        }
        
        if (club.getTrainingFacility() != null) {
            totalValue = totalValue.add(club.getTrainingFacility().getConstructionCost())
                                  .add(club.getTrainingFacility().getUpgradeValue());
        }
        
        if (club.getMedicalCenter() != null) {
            totalValue = totalValue.add(club.getMedicalCenter().getConstructionCost())
                                  .add(club.getMedicalCenter().getUpgradeValue());
        }
        
        if (club.getYouthAcademy() != null) {
            totalValue = totalValue.add(club.getYouthAcademy().getConstructionCost())
                                  .add(club.getYouthAcademy().getUpgradeValue());
        }
        
        return totalValue;
    }
    
    /**
     * Update stadium multipliers based on quality and features
     */
    private void updateStadiumMultipliers(Stadium stadium) {
        // Calculate atmosphere multiplier (affects player performance)
        double atmosphereBase = 1.0;
        atmosphereBase += (stadium.getPitchQuality() - 5) * 0.02; // -0.08 to +0.10
        atmosphereBase += (stadium.getFacilitiesQuality() - 5) * 0.01; // -0.04 to +0.05
        
        if (stadium.getHasRoof()) atmosphereBase += 0.05;
        if (stadium.getCapacity() > 50000) atmosphereBase += 0.05;
        if (stadium.getCapacity() > 70000) atmosphereBase += 0.05;
        
        stadium.setAtmosphereMultiplier(Math.max(0.8, Math.min(1.3, atmosphereBase)));
        
        // Calculate revenue multiplier (affects matchday income)
        double revenueBase = 1.0;
        revenueBase += (stadium.getFacilitiesQuality() - 5) * 0.03; // -0.12 to +0.15
        
        if (stadium.getHasVipBoxes()) revenueBase += 0.10;
        if (stadium.getHasMegastore()) revenueBase += 0.05;
        if (stadium.getHasMuseum()) revenueBase += 0.03;
        
        stadium.setRevenueMultiplier(Math.max(0.7, Math.min(1.5, revenueBase)));
    }
}
```

### API Endpoints

#### InfrastructureController
```java
@RestController
@RequestMapping("/api/infrastructure")
public class InfrastructureController {
    
    @Autowired
    private InfrastructureService infrastructureService;
    
    @GetMapping("/club/{clubId}/overview")
    public ResponseEntity<InfrastructureOverviewDTO> getInfrastructureOverview(@PathVariable Long clubId) {
        InfrastructureOverviewDTO overview = infrastructureService.getInfrastructureOverview(clubId);
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/club/{clubId}/upgrades/{facilityType}")
    public ResponseEntity<List<UpgradeOptionDTO>> getAvailableUpgrades(
            @PathVariable Long clubId,
            @PathVariable FacilityType facilityType) {
        List<UpgradeOptionDTO> upgrades = infrastructureService.getAvailableUpgrades(clubId, facilityType);
        return ResponseEntity.ok(upgrades);
    }
    
    @PostMapping("/club/{clubId}/upgrade/start")
    public ResponseEntity<FacilityUpgradeDTO> startUpgrade(
            @PathVariable Long clubId,
            @RequestBody StartUpgradeRequest request) {
        FacilityUpgrade upgrade = infrastructureService.startUpgrade(clubId, request);
        return ResponseEntity.ok(convertToDTO(upgrade));
    }
    
    @PostMapping("/upgrade/{upgradeId}/complete")
    public ResponseEntity<Void> completeUpgrade(@PathVariable Long upgradeId) {
        infrastructureService.completeUpgrade(upgradeId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/club/{clubId}/maintenance/schedule")
    public ResponseEntity<List<MaintenanceRecordDTO>> getMaintenanceSchedule(@PathVariable Long clubId) {
        List<MaintenanceRecord> maintenance = infrastructureService.getMaintenanceSchedule(clubId);
        return ResponseEntity.ok(maintenance.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/club/{clubId}/maintenance/schedule")
    public ResponseEntity<MaintenanceRecordDTO> scheduleMaintenance(
            @PathVariable Long clubId,
            @RequestBody ScheduleMaintenanceRequest request) {
        MaintenanceRecord maintenance = infrastructureService.scheduleMaintenance(clubId, request);
        return ResponseEntity.ok(convertToDTO(maintenance));
    }
}
```

### Frontend Implementation

#### InfrastructureDashboard Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getInfrastructureOverview, getAvailableUpgrades, startUpgrade } from '../services/api';

const InfrastructureDashboard = ({ clubId }) => {
    const [overview, setOverview] = useState(null);
    const [selectedFacility, setSelectedFacility] = useState(null);
    const [availableUpgrades, setAvailableUpgrades] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadInfrastructureData();
    }, [clubId]);

    const loadInfrastructureData = async () => {
        try {
            const response = await getInfrastructureOverview(clubId);
            setOverview(response.data);
        } catch (error) {
            console.error('Error loading infrastructure data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleFacilitySelect = async (facilityType) => {
        setSelectedFacility(facilityType);
        try {
            const response = await getAvailableUpgrades(clubId, facilityType);
            setAvailableUpgrades(response.data);
        } catch (error) {
            console.error('Error loading upgrades:', error);
        }
    };

    const handleStartUpgrade = async (upgrade) => {
        try {
            await startUpgrade(clubId, upgrade);
            loadInfrastructureData(); // Refresh data
            setAvailableUpgrades([]); // Clear upgrades
            setSelectedFacility(null);
        } catch (error) {
            console.error('Error starting upgrade:', error);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    };

    const getQualityColor = (quality) => {
        if (quality >= 8) return '#4caf50'; // Green
        if (quality >= 6) return '#ff9800'; // Orange
        if (quality >= 4) return '#ffc107'; // Yellow
        return '#f44336'; // Red
    };

    if (loading) return <div>Loading infrastructure overview...</div>;

    return (
        <div className="infrastructure-dashboard">
            <div className="dashboard-header">
                <h2>Infrastructure Management</h2>
                <div className="infrastructure-summary">
                    <div className="summary-item">
                        <span className="label">Total Value:</span>
                        <span className="value">{formatCurrency(overview.totalInfrastructureValue)}</span>
                    </div>
                    <div className="summary-item">
                        <span className="label">Monthly Maintenance:</span>
                        <span className="value negative">{formatCurrency(overview.totalMonthlyMaintenanceCost)}</span>
                    </div>
                </div>
            </div>

            <div className="facilities-grid">
                {/* Stadium */}
                <div className="facility-card" onClick={() => handleFacilitySelect('STADIUM')}>
                    <div className="facility-header">
                        <h3>Stadium</h3>
                        {overview.stadium && (
                            <span className="facility-name">{overview.stadium.name}</span>
                        )}
                    </div>
                    
                    {overview.stadium ? (
                        <div className="facility-details">
                            <div className="capacity">
                                <span className="label">Capacity:</span>
                                <span className="value">{overview.stadium.capacity.toLocaleString()}</span>
                            </div>
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Pitch Quality:</span>
                                    <div className="quality-bar">
                                        <div 
                                            className="quality-fill"
                                            style={{ 
                                                width: `${overview.stadium.pitchQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.stadium.pitchQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.stadium.pitchQuality}/10</span>
                                </div>
                                <div className="quality-item">
                                    <span>Facilities:</span>
                                    <div className="quality-bar">
                                        <div 
                                            className="quality-fill"
                                            style={{ 
                                                width: `${overview.stadium.facilitiesQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.stadium.facilitiesQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.stadium.facilitiesQuality}/10</span>
                                </div>
                            </div>
                            <div className="facility-features">
                                {overview.stadium.hasRoof && <span className="feature">Roof</span>}
                                {overview.stadium.hasUndersoilHeating && <span className="feature">Heating</span>}
                                {overview.stadium.hasVipBoxes && <span className="feature">VIP Boxes</span>}
                                {overview.stadium.hasMegastore && <span className="feature">Megastore</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.stadium.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No stadium built</p>
                            <button className="btn-primary">Build Stadium</button>
                        </div>
                    )}
                </div>

                {/* Training Facility */}
                <div className="facility-card" onClick={() => handleFacilitySelect('TRAINING_FACILITY')}>
                    <div className="facility-header">
                        <h3>Training Facility</h3>
                        {overview.trainingFacility && (
                            <span className="facility-name">{overview.trainingFacility.name}</span>
                        )}
                    </div>
                    
                    {overview.trainingFacility ? (
                        <div className="facility-details">
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Overall Quality:</span>
                                    <div className="quality-bar">
                                        <div 
                                            className="quality-fill"
                                            style={{ 
                                                width: `${overview.trainingFacility.overallQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.trainingFacility.overallQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.trainingFacility.overallQuality}/10</span>
                                </div>
                            </div>
                            <div className="training-bonuses">
                                <div className="bonus">
                                    Physical: +{(overview.trainingFacility.physicalTrainingBonus * 100).toFixed(1)}%
                                </div>
                                <div className="bonus">
                                    Technical: +{(overview.trainingFacility.technicalTrainingBonus * 100).toFixed(1)}%
                                </div>
                                <div className="bonus">
                                    Tactical: +{(overview.trainingFacility.tacticalTrainingBonus * 100).toFixed(1)}%
                                </div>
                            </div>
                            <div className="facility-features">
                                <span className="feature">{overview.trainingFacility.numberOfPitches} Pitches</span>
                                {overview.trainingFacility.hasIndoorFacilities && <span className="feature">Indoor</span>}
                                {overview.trainingFacility.hasHydrotherapy && <span className="feature">Hydrotherapy</span>}
                                {overview.trainingFacility.hasVideoAnalysis && <span className="feature">Video Analysis</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.trainingFacility.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No training facility built</p>
                            <button className="btn-primary">Build Training Facility</button>
                        </div>
                    )}
                </div>

                {/* Medical Center */}
                <div className="facility-card" onClick={() => handleFacilitySelect('MEDICAL_CENTER')}>
                    <div className="facility-header">
                        <h3>Medical Center</h3>
                        {overview.medicalCenter && (
                            <span className="facility-name">{overview.medicalCenter.name}</span>
                        )}
                    </div>
                    
                    {overview.medicalCenter ? (
                        <div className="facility-details">
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Overall Quality:</span>
                                    <div className="quality-bar">
                                        <div 
                                            className="quality-fill"
                                            style={{ 
                                                width: `${overview.medicalCenter.overallQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.medicalCenter.overallQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.medicalCenter.overallQuality}/10</span>
                                </div>
                            </div>
                            <div className="medical-bonuses">
                                <div className="bonus">
                                    Injury Prevention: +{(overview.medicalCenter.injuryPreventionBonus * 100).toFixed(1)}%
                                </div>
                                <div className="bonus">
                                    Recovery Speed: +{(overview.medicalCenter.recoverySpeedBonus * 100).toFixed(1)}%
                                </div>
                                <div className="bonus">
                                    Fitness Maintenance: +{(overview.medicalCenter.fitnessMaintenanceBonus * 100).toFixed(1)}%
                                </div>
                            </div>
                            <div className="facility-features">
                                <span className="feature">{overview.medicalCenter.numberOfDoctors} Doctors</span>
                                {overview.medicalCenter.hasMriScanner && <span className="feature">MRI</span>}
                                {overview.medicalCenter.hasCryotherapy && <span className="feature">Cryotherapy</span>}
                                {overview.medicalCenter.hasHyperbaricChamber && <span className="feature">Hyperbaric</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.medicalCenter.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No medical center built</p>
                            <button className="btn-primary">Build Medical Center</button>
                        </div>
                    )}
                </div>

                {/* Youth Academy */}
                <div className="facility-card" onClick={() => handleFacilitySelect('YOUTH_ACADEMY')}>
                    <div className="facility-header">
                        <h3>Youth Academy</h3>
                        {overview.youthAcademy && (
                            <span className="facility-name">{overview.youthAcademy.name}</span>
                        )}
                    </div>
                    
                    {overview.youthAcademy ? (
                        <div className="facility-details">
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Overall Quality:</span>
                                    <div className="quality-bar">
                                        <div 
                                            className="quality-fill"
                                            style={{ 
                                                width: `${overview.youthAcademy.overallQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.youthAcademy.overallQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.youthAcademy.overallQuality}/10</span>
                                </div>
                            </div>
                            <div className="youth-bonuses">
                                <div className="bonus">
                                    Talent Generation: +{(overview.youthAcademy.talentGenerationBonus * 100).toFixed(1)}%
                                </div>
                                <div className="bonus">
                                    Development Speed: +{(overview.youthAcademy.developmentSpeedBonus * 100).toFixed(1)}%
                                </div>
                                <div className="bonus">
                                    Retention: +{(overview.youthAcademy.retentionBonus * 100).toFixed(1)}%
                                </div>
                            </div>
                            <div className="facility-features">
                                <span className="feature">Capacity: {overview.youthAcademy.maxYouthPlayers}</span>
                                <span className="feature">{overview.youthAcademy.numberOfCoaches} Coaches</span>
                                {overview.youthAcademy.hasEducationCenter && <span className="feature">Education</span>}
                                {overview.youthAcademy.hasScoutingNetwork && <span className="feature">Scouting</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.youthAcademy.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No youth academy built</p>
                            <button className="btn-primary">Build Youth Academy</button>
                        </div>
                    )}
                </div>
            </div>

            {/* Ongoing Upgrades */}
            {overview.ongoingUpgrades.length > 0 && (
                <div className="ongoing-upgrades">
                    <h3>Ongoing Upgrades ({overview.ongoingUpgrades.length})</h3>
                    <div className="upgrades-list">
                        {overview.ongoingUpgrades.map(upgrade => (
                            <div key={upgrade.id} className="upgrade-card">
                                <div className="upgrade-info">
                                    <h4>{upgrade.upgradeName}</h4>
                                    <p>{upgrade.description}</p>
                                    <span className="facility-type">{upgrade.facilityType}</span>
                                </div>
                                <div className="upgrade-progress">
                                    <div className="progress-bar">
                                        <div 
                                            className="progress-fill"
                                            style={{ width: `${calculateUpgradeProgress(upgrade)}%` }}
                                        ></div>
                                    </div>
                                    <span className="progress-text">
                                        {calculateRemainingDays(upgrade.plannedCompletionDate)} days remaining
                                    </span>
                                </div>
                                <div className="upgrade-cost">
                                    {formatCurrency(upgrade.cost)}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Available Upgrades Modal */}
            {selectedFacility && availableUpgrades.length > 0 && (
                <div className="upgrades-modal">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h3>Available Upgrades - {selectedFacility.replace('_', ' ')}</h3>
                            <button onClick={() => setSelectedFacility(null)}>×</button>
                        </div>
                        <div className="upgrades-list">
                            {availableUpgrades.map((upgrade, index) => (
                                <div key={index} className="upgrade-option">
                                    <div className="upgrade-details">
                                        <h4>{upgrade.name}</h4>
                                        <p>{upgrade.description}</p>
                                        <div className="upgrade-effects">
                                            <strong>Effects:</strong> {upgrade.effects}
                                        </div>
                                        {upgrade.requirements && (
                                            <div className="upgrade-requirements">
                                                <strong>Requirements:</strong> {upgrade.requirements}
                                            </div>
                                        )}
                                    </div>
                                    <div className="upgrade-specs">
                                        <div className="spec">
                                            <span className="label">Cost:</span>
                                            <span className="value">{formatCurrency(upgrade.cost)}</span>
                                        </div>
                                        <div className="spec">
                                            <span className="label">Duration:</span>
                                            <span className="value">{upgrade.durationDays} days</span>
                                        </div>
                                        <button 
                                            onClick={() => handleStartUpgrade(upgrade)}
                                            className="btn-primary"
                                        >
                                            Start Upgrade
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const calculateUpgradeProgress = (upgrade) => {
    const start = new Date(upgrade.startDate);
    const planned = new Date(upgrade.plannedCompletionDate);
    const now = new Date();
    
    const total = planned - start;
    const elapsed = now - start;
    
    return Math.min(Math.max((elapsed / total) * 100, 0), 100);
};

const calculateRemainingDays = (completionDate) => {
    const completion = new Date(completionDate);
    const now = new Date();
    const diff = completion - now;
    
    return Math.max(Math.ceil(diff / (1000 * 60 * 60 * 24)), 0);
};

export default InfrastructureDashboard;
```

## Implementation Notes

1. **Facility Bonuses**: Each facility type provides specific bonuses that affect gameplay
2. **Upgrade System**: Progressive upgrade system with prerequisites and dependencies
3. **Maintenance Costs**: Ongoing costs that scale with facility quality and features
4. **Construction Time**: Realistic construction/upgrade durations
5. **Financial Integration**: All costs integrated with the financial management system
6. **Quality Degradation**: Facilities degrade over time without proper maintenance

## Dependencies

- Financial Management System (for payments and costs)
- Club entity (for facility ownership)
- Season system (for maintenance scheduling)
- Player and Training systems (for bonus application)
- Match system (for stadium effects)

## Testing Strategy

1. **Unit Tests**: Test upgrade calculations, bonus applications, maintenance scheduling
2. **Integration Tests**: Test facility interactions with other systems
3. **Performance Tests**: Test with multiple concurrent upgrades
4. **User Acceptance Tests**: Test complete facility management workflow