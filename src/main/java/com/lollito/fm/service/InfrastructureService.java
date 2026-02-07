package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.FacilityUpgrade;
import com.lollito.fm.model.MaintenanceRecord;
import com.lollito.fm.model.MaintenanceStatus;
import com.lollito.fm.model.MedicalCenter;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.TrainingFacility;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.UpgradeStatus;
import com.lollito.fm.model.UpgradeType;
import com.lollito.fm.model.YouthAcademy;
import com.lollito.fm.mapper.FacilityMapper;
import com.lollito.fm.mapper.StadiumMapper;
import com.lollito.fm.model.dto.FacilityUpgradeDTO;
import com.lollito.fm.model.dto.InfrastructureOverviewDTO;
import com.lollito.fm.model.dto.MaintenanceRecordDTO;
import com.lollito.fm.model.dto.ScheduleMaintenanceRequest;
import com.lollito.fm.model.dto.StartUpgradeRequest;
import com.lollito.fm.model.dto.UpgradeOptionDTO;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.FacilityUpgradeRepository;
import com.lollito.fm.repository.rest.MaintenanceRecordRepository;
import com.lollito.fm.repository.rest.MedicalCenterRepository;
import com.lollito.fm.repository.rest.StadiumRepository;
import com.lollito.fm.repository.rest.TrainingFacilityRepository;
import com.lollito.fm.repository.rest.YouthAcademyRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Slf4j
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

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private StadiumMapper stadiumMapper;

    @Autowired
    private FacilityMapper facilityMapper;

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
            .stadium(stadiumMapper.toDto(stadium))
            .trainingFacility(facilityMapper.toDto(trainingFacility))
            .medicalCenter(facilityMapper.toDto(medicalCenter))
            .youthAcademy(facilityMapper.toDto(youthAcademy))
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
    public FacilityUpgradeDTO startUpgrade(Long clubId, StartUpgradeRequest request) {
        Club club = clubService.findById(clubId);

        // Validate club has sufficient funds
        if (club.getFinance().getBalance().compareTo(request.getCost()) < 0) {
            throw new RuntimeException("Club does not have sufficient funds for this upgrade");
        }

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

        return convertToDTO(upgrade);
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

        // Apply upgrade effects to facility
        applyUpgradeEffects(upgrade);

        facilityUpgradeRepository.save(upgrade);
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

    private void applyStadiumUpgrade(FacilityUpgrade upgrade) {
        Stadium stadium = stadiumRepository.findById(upgrade.getFacilityId())
            .orElseThrow(() -> new EntityNotFoundException("Stadium not found"));

        if (upgrade.getUpgradeType() == UpgradeType.CAPACITY_EXPANSION) {
            try {
                 int increase = Integer.parseInt(upgrade.getBonusEffects());
                 stadium.setCapacity(stadium.getCapacity() + increase);
            } catch (NumberFormatException e) {
                // Fallback or log
            }
        } else if (upgrade.getUpgradeType() == UpgradeType.QUALITY_IMPROVEMENT) {
             stadium.setPitchQuality(Math.min(stadium.getPitchQuality() + upgrade.getQualityImprovement(), 10));
             stadium.setFacilitiesQuality(Math.min(stadium.getFacilitiesQuality() + upgrade.getQualityImprovement(), 10));
        } else if (upgrade.getUpgradeType() == UpgradeType.FEATURE_ADDITION) {
            if (upgrade.getUpgradeName().contains("Heating")) stadium.setHasUndersoilHeating(true);
            if (upgrade.getUpgradeName().contains("Roof")) stadium.setHasRoof(true);
        }

        if (upgrade.getMaintenanceCostIncrease() != null) {
            BigDecimal currentCost = stadium.getMaintenanceCost();
            BigDecimal increase = currentCost.multiply(BigDecimal.valueOf(upgrade.getMaintenanceCostIncrease()));
            stadium.setMaintenanceCost(currentCost.add(increase));
        }

        stadium.setUpgradeValue(stadium.getUpgradeValue().add(upgrade.getCost()));
        stadium.setLastUpgradeDate(LocalDate.now());

        updateStadiumMultipliers(stadium);

        stadiumRepository.save(stadium);
    }

    private void applyTrainingFacilityUpgrade(FacilityUpgrade upgrade) {
        if (upgrade.getUpgradeType() == UpgradeType.CONSTRUCTION) {
            TrainingFacility facility = TrainingFacility.builder()
                .name(upgrade.getClub().getName() + " Training Center")
                .overallQuality(1)
                .pitchQuality(1)
                .gymQuality(1)
                .numberOfPitches(1)
                .hasIndoorFacilities(false)
                .hasHydrotherapy(false)
                .hasVideoAnalysis(false)
                .physicalTrainingBonus(0.01)
                .technicalTrainingBonus(0.01)
                .maintenanceCost(BigDecimal.valueOf(2000))
                .constructionCost(upgrade.getCost())
                .upgradeValue(BigDecimal.ZERO)
                .build();

            facility = trainingFacilityRepository.save(facility);

            Club club = upgrade.getClub();
            club.setTrainingFacility(facility);
            clubRepository.save(club);

            upgrade.setFacilityId(facility.getId());
            return;
        }

        TrainingFacility facility = trainingFacilityRepository.findById(upgrade.getFacilityId())
             .orElseThrow(() -> new EntityNotFoundException("Training Facility not found"));

        if (upgrade.getUpgradeType() == UpgradeType.QUALITY_IMPROVEMENT) {
            facility.setOverallQuality(Math.min(facility.getOverallQuality() + upgrade.getQualityImprovement(), 10));
            facility.setPitchQuality(Math.min(facility.getPitchQuality() + upgrade.getQualityImprovement(), 10));
            facility.setGymQuality(Math.min(facility.getGymQuality() + upgrade.getQualityImprovement(), 10));
        } else if (upgrade.getUpgradeType() == UpgradeType.CAPACITY_EXPANSION) {
            facility.setNumberOfPitches(facility.getNumberOfPitches() + 1);
        } else if (upgrade.getUpgradeType() == UpgradeType.FEATURE_ADDITION) {
             if (upgrade.getUpgradeName().contains("Indoor")) facility.setHasIndoorFacilities(true);
             if (upgrade.getUpgradeName().contains("Hydrotherapy")) facility.setHasHydrotherapy(true);
        }

        if (upgrade.getMaintenanceCostIncrease() != null) {
            BigDecimal currentCost = facility.getMaintenanceCost();
            BigDecimal increase = currentCost.multiply(BigDecimal.valueOf(upgrade.getMaintenanceCostIncrease()));
            facility.setMaintenanceCost(currentCost.add(increase));
        }

        facility.setUpgradeValue(facility.getUpgradeValue().add(upgrade.getCost()));
        facility.setLastUpgradeDate(LocalDate.now());
        trainingFacilityRepository.save(facility);
    }

    private void applyMedicalCenterUpgrade(FacilityUpgrade upgrade) {
        if (upgrade.getUpgradeType() == UpgradeType.CONSTRUCTION) {
            MedicalCenter facility = MedicalCenter.builder()
                .name(upgrade.getClub().getName() + " Medical Center")
                .overallQuality(1)
                .numberOfDoctors(1)
                .numberOfPhysiotherapists(1)
                .hasMriScanner(false)
                .hasCryotherapy(false)
                .hasHyperbaricChamber(false)
                .injuryPreventionBonus(0.01)
                .recoverySpeedBonus(0.01)
                .maintenanceCost(BigDecimal.valueOf(2500))
                .constructionCost(upgrade.getCost())
                .upgradeValue(BigDecimal.ZERO)
                .build();

            facility = medicalCenterRepository.save(facility);

            Club club = upgrade.getClub();
            club.setMedicalCenter(facility);
            clubRepository.save(club);

            upgrade.setFacilityId(facility.getId());
            return;
        }

        MedicalCenter facility = medicalCenterRepository.findById(upgrade.getFacilityId())
             .orElseThrow(() -> new EntityNotFoundException("Medical Center not found"));

        if (upgrade.getUpgradeType() == UpgradeType.QUALITY_IMPROVEMENT) {
             facility.setOverallQuality(Math.min(facility.getOverallQuality() + upgrade.getQualityImprovement(), 10));
        } else if (upgrade.getUpgradeType() == UpgradeType.FEATURE_ADDITION) {
             if (upgrade.getUpgradeName().contains("MRI")) facility.setHasMriScanner(true);
        }

        if (upgrade.getMaintenanceCostIncrease() != null) {
            BigDecimal currentCost = facility.getMaintenanceCost();
            BigDecimal increase = currentCost.multiply(BigDecimal.valueOf(upgrade.getMaintenanceCostIncrease()));
            facility.setMaintenanceCost(currentCost.add(increase));
        }

        facility.setUpgradeValue(facility.getUpgradeValue().add(upgrade.getCost()));
        facility.setLastUpgradeDate(LocalDate.now());
        medicalCenterRepository.save(facility);
    }

    private void applyYouthAcademyUpgrade(FacilityUpgrade upgrade) {
        if (upgrade.getUpgradeType() == UpgradeType.CONSTRUCTION) {
            YouthAcademy facility = YouthAcademy.builder()
                .name(upgrade.getClub().getName() + " Youth Academy")
                .overallQuality(1)
                .numberOfCoaches(1)
                .maxYouthPlayers(15)
                .hasEducationCenter(false)
                .hasScoutingNetwork(false)
                .talentGenerationBonus(0.01)
                .maintenanceCost(BigDecimal.valueOf(1500))
                .constructionCost(upgrade.getCost())
                .upgradeValue(BigDecimal.ZERO)
                .build();

            facility = youthAcademyRepository.save(facility);

            Club club = upgrade.getClub();
            club.setYouthAcademy(facility);
            clubRepository.save(club);

            upgrade.setFacilityId(facility.getId());
            return;
        }

        YouthAcademy facility = youthAcademyRepository.findById(upgrade.getFacilityId())
             .orElseThrow(() -> new EntityNotFoundException("Youth Academy not found"));

        if (upgrade.getUpgradeType() == UpgradeType.QUALITY_IMPROVEMENT) {
             facility.setOverallQuality(Math.min(facility.getOverallQuality() + upgrade.getQualityImprovement(), 10));
        } else if (upgrade.getUpgradeType() == UpgradeType.CAPACITY_EXPANSION) {
             facility.setMaxYouthPlayers(facility.getMaxYouthPlayers() + 5);
        }

        if (upgrade.getMaintenanceCostIncrease() != null) {
            BigDecimal currentCost = facility.getMaintenanceCost();
            BigDecimal increase = currentCost.multiply(BigDecimal.valueOf(upgrade.getMaintenanceCostIncrease()));
            facility.setMaintenanceCost(currentCost.add(increase));
        }

        facility.setUpgradeValue(facility.getUpgradeValue().add(upgrade.getCost()));
        facility.setLastUpgradeDate(LocalDate.now());
        youthAcademyRepository.save(facility);
    }

    @Scheduled(initialDelayString = "${fm.scheduling.infrastructure.initial-delay}", fixedRateString = "${fm.scheduling.infrastructure.fixed-rate}")
    @Transactional
    public void processMonthlyMaintenance() {
        log.info("Starting processMonthlyMaintenance...");
        List<Club> allClubs = clubService.findAll();

        for (Club club : allClubs) {
            processClubMaintenance(club);
        }
        log.info("Finished processMonthlyMaintenance.");
    }

    private void processClubMaintenance(Club club) {
        BigDecimal totalMaintenanceCost = calculateTotalMaintenanceCost(club);

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

    private BigDecimal calculateTotalMaintenanceCost(Club club) {
        BigDecimal total = BigDecimal.ZERO;
        if (club.getStadium() != null) total = total.add(club.getStadium().getMaintenanceCost());
        if (club.getTrainingFacility() != null) total = total.add(club.getTrainingFacility().getMaintenanceCost());
        if (club.getMedicalCenter() != null) total = total.add(club.getMedicalCenter().getMaintenanceCost());
        if (club.getYouthAcademy() != null) total = total.add(club.getYouthAcademy().getMaintenanceCost());
        return total;
    }

    private BigDecimal calculateTotalInfrastructureValue(Club club) {
        BigDecimal total = BigDecimal.ZERO;
        if (club.getStadium() != null) total = total.add(club.getStadium().getConstructionCost()).add(club.getStadium().getUpgradeValue());
        if (club.getTrainingFacility() != null) total = total.add(club.getTrainingFacility().getConstructionCost()).add(club.getTrainingFacility().getUpgradeValue());
        if (club.getMedicalCenter() != null) total = total.add(club.getMedicalCenter().getConstructionCost()).add(club.getMedicalCenter().getUpgradeValue());
        if (club.getYouthAcademy() != null) total = total.add(club.getYouthAcademy().getConstructionCost()).add(club.getYouthAcademy().getUpgradeValue());
        return total;
    }

    private void updateStadiumMultipliers(Stadium stadium) {
        double atmosphereBase = 1.0;
        atmosphereBase += (stadium.getPitchQuality() - 5) * 0.02;
        atmosphereBase += (stadium.getFacilitiesQuality() - 5) * 0.01;

        if (Boolean.TRUE.equals(stadium.getHasRoof())) atmosphereBase += 0.05;
        if (stadium.getCapacity() > 50000) atmosphereBase += 0.05;
        if (stadium.getCapacity() > 70000) atmosphereBase += 0.05;

        stadium.setAtmosphereMultiplier(Math.max(0.8, Math.min(1.3, atmosphereBase)));

        double revenueBase = 1.0;
        revenueBase += (stadium.getFacilitiesQuality() - 5) * 0.03;

        if (Boolean.TRUE.equals(stadium.getHasVipBoxes())) revenueBase += 0.10;
        if (Boolean.TRUE.equals(stadium.getHasMegastore())) revenueBase += 0.05;
        if (Boolean.TRUE.equals(stadium.getHasMuseum())) revenueBase += 0.03;

        stadium.setRevenueMultiplier(Math.max(0.7, Math.min(1.5, revenueBase)));
    }

    private List<UpgradeOptionDTO> getStadiumUpgradeOptions(Stadium stadium) {
        List<UpgradeOptionDTO> options = new ArrayList<>();
        // Stadium is mandatory, shouldn't be null but just in case
        if (stadium == null) return options;

        if (stadium.getCapacity() < 80000) {
            options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.CAPACITY_EXPANSION)
                .name("Capacity Expansion (+5,000 seats)")
                .description("Expand stadium capacity by 5,000 seats")
                .cost(BigDecimal.valueOf(2500000))
                .durationDays(180)
                .effects("5000")
                .requirements("Stadium quality level 5+")
                .build());
        }

        if (stadium.getPitchQuality() < 10) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.QUALITY_IMPROVEMENT)
                .name("Pitch Quality Upgrade")
                .description("Improve pitch quality")
                .cost(BigDecimal.valueOf(500000))
                .durationDays(30)
                .effects("Pitch Quality +1")
                .requirements("None")
                .build());
        }

        if (Boolean.FALSE.equals(stadium.getHasUndersoilHeating())) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.FEATURE_ADDITION)
                .name("Undersoil Heating System")
                .description("Install undersoil heating")
                .cost(BigDecimal.valueOf(1000000))
                .durationDays(90)
                .effects("Has Heating")
                .requirements("Quality 6+")
                .build());
        }

        return options;
    }

    private List<UpgradeOptionDTO> getTrainingFacilityUpgradeOptions(TrainingFacility facility) {
         List<UpgradeOptionDTO> options = new ArrayList<>();
         if (facility == null) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.CONSTRUCTION)
                .name("Construct Training Center")
                .description("Build a basic training center for your team")
                .cost(BigDecimal.valueOf(2000000))
                .durationDays(120)
                .effects("Enables training bonuses")
                .build());
             return options;
         }

         if (facility.getOverallQuality() < 10) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.QUALITY_IMPROVEMENT)
                .name("Upgrade Training Equipment")
                .description("New gym and training equipment")
                .cost(BigDecimal.valueOf(300000))
                .durationDays(45)
                .effects("Quality +1")
                .build());
         }
         return options;
    }

    private List<UpgradeOptionDTO> getMedicalCenterUpgradeOptions(MedicalCenter facility) {
         List<UpgradeOptionDTO> options = new ArrayList<>();
         if (facility == null) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.CONSTRUCTION)
                .name("Construct Medical Center")
                .description("Build a medical center to treat injuries")
                .cost(BigDecimal.valueOf(1500000))
                .durationDays(90)
                .effects("Enables injury treatment")
                .build());
             return options;
         }
         if (facility.getOverallQuality() < 10) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.QUALITY_IMPROVEMENT)
                .name("Upgrade Medical Equipment")
                .description("New diagnostic tools")
                .cost(BigDecimal.valueOf(400000))
                .durationDays(60)
                .effects("Quality +1")
                .build());
         }
         return options;
    }

    private List<UpgradeOptionDTO> getYouthAcademyUpgradeOptions(YouthAcademy facility) {
         List<UpgradeOptionDTO> options = new ArrayList<>();
         if (facility == null) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.CONSTRUCTION)
                .name("Construct Youth Academy")
                .description("Build an academy to recruit young players")
                .cost(BigDecimal.valueOf(1000000))
                .durationDays(150)
                .effects("Enables youth recruitment")
                .build());
             return options;
         }
         if (facility.getOverallQuality() < 10) {
             options.add(UpgradeOptionDTO.builder()
                .upgradeType(UpgradeType.QUALITY_IMPROVEMENT)
                .name("Upgrade Academy Facilities")
                .description("Better classrooms and pitches")
                .cost(BigDecimal.valueOf(350000))
                .durationDays(60)
                .effects("Quality +1")
                .build());
         }
         return options;
    }

    private FacilityUpgradeDTO convertToDTO(FacilityUpgrade u) {
        return FacilityUpgradeDTO.builder()
            .id(u.getId())
            .facilityType(u.getFacilityType())
            .facilityId(u.getFacilityId())
            .upgradeType(u.getUpgradeType())
            .upgradeName(u.getUpgradeName())
            .description(u.getDescription())
            .cost(u.getCost())
            .durationDays(u.getDurationDays())
            .qualityImprovement(u.getQualityImprovement())
            .status(u.getStatus())
            .startDate(u.getStartDate())
            .plannedCompletionDate(u.getPlannedCompletionDate())
            .build();
    }

    private MaintenanceRecordDTO convertToDTO(MaintenanceRecord m) {
        return MaintenanceRecordDTO.builder()
            .id(m.getId())
            .facilityType(m.getFacilityType())
            .facilityId(m.getFacilityId())
            .maintenanceType(m.getMaintenanceType())
            .description(m.getDescription())
            .cost(m.getCost())
            .status(m.getStatus())
            .scheduledDate(m.getScheduledDate())
            .build();
    }

    public List<MaintenanceRecord> getMaintenanceSchedule(Long clubId) {
        Club club = clubService.findById(clubId);
        return maintenanceRecordRepository.findByClubAndStatusAndScheduledDateAfter(club, MaintenanceStatus.SCHEDULED, LocalDate.now());
    }

    public MaintenanceRecord scheduleMaintenance(Long clubId, ScheduleMaintenanceRequest request) {
        Club club = clubService.findById(clubId);
        MaintenanceRecord record = MaintenanceRecord.builder()
            .club(club)
            .facilityType(request.getFacilityType())
            .facilityId(request.getFacilityId())
            .maintenanceType(request.getMaintenanceType())
            .description(request.getDescription())
            .cost(request.getCost())
            .status(MaintenanceStatus.SCHEDULED)
            .scheduledDate(request.getScheduledDate())
            .contractorName(request.getContractorName())
            .build();
        return maintenanceRecordRepository.save(record);
    }
}
