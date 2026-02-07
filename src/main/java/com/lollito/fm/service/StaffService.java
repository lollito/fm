package com.lollito.fm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lollito.fm.dto.StaffBonusesDTO;
import com.lollito.fm.dto.request.HireStaffRequest;
import com.lollito.fm.dto.request.RenewContractRequest;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.ContractStatus;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Staff;
import com.lollito.fm.model.StaffContract;
import com.lollito.fm.model.StaffRole;
import com.lollito.fm.model.StaffSpecialization;
import com.lollito.fm.model.StaffStatus;
import com.lollito.fm.repository.StaffContractRepository;
import com.lollito.fm.repository.StaffRepository;
import com.lollito.fm.utils.RandomUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Slf4j
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffContractRepository staffContractRepository;

    @Autowired
    private ClubService clubService;

    @Autowired
    private NameService nameService;

    @Autowired
    private CountryService countryService;

    public List<Staff> generateAvailableStaff(StaffRole role, int count) {
        List<Staff> availableStaff = new ArrayList<>();
        List<Country> countries = countryService.findAll();
        List<String> names = nameService.getNames();
        List<String> surnames = nameService.getSurnames();

        for (int i = 0; i < count; i++) {
            Staff staff = generateRandomStaff(role, countries, names, surnames);
            availableStaff.add(staff);
        }

        return staffRepository.saveAll(availableStaff);
    }

    private Staff generateRandomStaff(StaffRole role, List<Country> countries, List<String> names, List<String> surnames) {
        String name = RandomUtils.randomValueFromList(names);
        String surname = RandomUtils.randomValueFromList(surnames);
        LocalDate birth = generateStaffBirthDate();

        int ability = RandomUtils.randomValue(1, 20);
        int reputation = RandomUtils.randomValue(1, 20);
        int experience = RandomUtils.randomValue(1, 40);

        BigDecimal salary = calculateStaffSalary(role, ability, reputation, experience);

        Staff staff = Staff.builder()
            .name(name)
            .surname(surname)
            .birth(birth)
            .role(role)
            .specialization(generateSpecialization(role))
            .ability(ability)
            .reputation(reputation)
            .experience(experience)
            .monthlySalary(salary)
            .status(StaffStatus.ACTIVE)
            .nationality(RandomUtils.randomValueFromList(countries))
            .description(generateStaffDescription(role, ability, experience))
            .build();

        calculateStaffBonuses(staff);

        return staff;
    }

    private LocalDate generateStaffBirthDate() {
        // Age between 25 and 65
        int age = RandomUtils.randomValue(25, 65);
        return LocalDate.now().minusYears(age).minusDays(RandomUtils.randomValue(0, 364));
    }

    private StaffSpecialization generateSpecialization(StaffRole role) {
        // Simple mapping based on role type
        List<StaffSpecialization> candidates = new ArrayList<>();

        if (role.name().contains("COACH")) {
            candidates.add(StaffSpecialization.ATTACKING_PLAY);
            candidates.add(StaffSpecialization.DEFENSIVE_PLAY);
            candidates.add(StaffSpecialization.SET_PIECES);
            candidates.add(StaffSpecialization.YOUTH_DEVELOPMENT);
            candidates.add(StaffSpecialization.PLAYER_DEVELOPMENT);
        } else if (role.name().contains("PHYSIO") || role.name().contains("DOCTOR")) {
            candidates.add(StaffSpecialization.INJURY_PREVENTION);
            candidates.add(StaffSpecialization.REHABILITATION);
            candidates.add(StaffSpecialization.SPORTS_PSYCHOLOGY);
        } else if (role.name().contains("SCOUT") || role.name().contains("ANALYST")) {
            candidates.add(StaffSpecialization.DOMESTIC_SCOUTING);
            candidates.add(StaffSpecialization.INTERNATIONAL_SCOUTING);
            candidates.add(StaffSpecialization.YOUTH_SCOUTING);
            candidates.add(StaffSpecialization.OPPOSITION_ANALYSIS);
        }

        if (candidates.isEmpty()) return StaffSpecialization.PLAYER_DEVELOPMENT;
        return RandomUtils.randomValueFromList(candidates);
    }

    private String generateStaffDescription(StaffRole role, int ability, int experience) {
        return String.format("A %s %s with %d years of experience.",
            ability > 15 ? "world-class" : (ability > 10 ? "competent" : "developing"),
            role.getDisplayName(), experience);
    }

    private void calculateStaffBonuses(Staff staff) {
        double abilityMultiplier = staff.getAbility() / 20.0;

        switch (staff.getRole()) {
            case HEAD_COACH:
            case ASSISTANT_COACH:
                staff.setMotivationBonus(abilityMultiplier * 0.2);
                staff.setTrainingBonus(abilityMultiplier * 0.3);
                break;
            case FITNESS_COACH:
                staff.setTrainingBonus(abilityMultiplier * 0.25);
                staff.setInjuryPreventionBonus(abilityMultiplier * 0.3);
                break;
            case GOALKEEPING_COACH:
                staff.setTrainingBonus(abilityMultiplier * 0.4);
                break;
            case YOUTH_COACH:
                staff.setTrainingBonus(abilityMultiplier * 0.2);
                break;
            case HEAD_PHYSIO:
            case PHYSIO:
                staff.setInjuryPreventionBonus(abilityMultiplier * 0.4);
                staff.setRecoveryBonus(abilityMultiplier * 0.5);
                break;
            case DOCTOR:
                staff.setInjuryPreventionBonus(abilityMultiplier * 0.3);
                staff.setRecoveryBonus(abilityMultiplier * 0.6);
                break;
            case HEAD_SCOUT:
            case SCOUT:
                staff.setScoutingBonus(abilityMultiplier * 0.4);
                break;
            case ANALYST:
                staff.setTrainingBonus(abilityMultiplier * 0.15);
                staff.setScoutingBonus(abilityMultiplier * 0.25);
                break;
        }
    }

    @Transactional
    public StaffContract hireStaff(Long clubId, Long staffId, HireStaffRequest request) {
        Club club = clubService.findById(clubId);
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (staff.getClub() != null) {
            throw new IllegalStateException("Staff member already employed");
        }

        if (club.getFinance() != null) {
            BigDecimal currentBalance = club.getFinance().getBalance();
            BigDecimal cost = request.getSigningBonus().add(staff.getMonthlySalary());
            if (currentBalance.compareTo(cost) < 0) {
                 // throw new IllegalStateException("Cannot afford staff salary");
            }
            if (request.getSigningBonus().compareTo(BigDecimal.ZERO) > 0) {
                club.getFinance().setBalance(
                    club.getFinance().getBalance().subtract(request.getSigningBonus())
                );
            }
        }

        StaffContract contract = StaffContract.builder()
            .staff(staff)
            .club(club)
            .monthlySalary(staff.getMonthlySalary())
            .signingBonus(request.getSigningBonus())
            .performanceBonus(request.getPerformanceBonus())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(request.getContractYears()))
            .status(ContractStatus.ACTIVE)
            .terminationFee(calculateTerminationFee(staff, request.getContractYears()))
            .build();

        staff.setClub(club);
        staff.setContractStart(contract.getStartDate());
        staff.setContractEnd(contract.getEndDate());

        staffRepository.save(staff);
        return staffContractRepository.save(contract);
    }

    @Transactional
    public void fireStaff(Long staffId, String reason) {
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (staff.getClub() == null) {
            throw new IllegalStateException("Staff member not employed");
        }

        StaffContract contract = staffContractRepository.findByStaffAndStatus(staff, ContractStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Active contract not found"));

        BigDecimal terminationFee = contract.getTerminationFee();

        Club club = staff.getClub();
        if (club.getFinance() != null) {
            club.getFinance().setBalance(
                club.getFinance().getBalance().subtract(terminationFee)
            );
        }

        staff.setClub(null);
        staff.setStatus(StaffStatus.TERMINATED);
        contract.setStatus(ContractStatus.TERMINATED);

        staffRepository.save(staff);
        staffContractRepository.save(contract);
    }

    public StaffBonusesDTO calculateClubStaffBonuses(Long clubId) {
        Club club = clubService.findById(clubId);
        List<Staff> activeStaff = club.getActiveStaff();

        double totalMotivationBonus = 0.0;
        double totalTrainingBonus = 0.0; // Keep generic training bonus for backward compatibility or general fitness
        double totalInjuryPreventionBonus = 0.0;
        double totalRecoveryBonus = 0.0;
        double totalScoutingBonus = 0.0;

        double totalGoalkeepingBonus = 0.0;
        double totalDefendingBonus = 0.0;
        double totalAttackingBonus = 0.0;
        double totalFitnessBonus = 0.0;
        double totalTacticalBonus = 0.0;

        for (Staff staff : activeStaff) {
            double abilityMultiplier = staff.getAbility() / 20.0;

            // Existing bonuses accumulation
            if (staff.getMotivationBonus() != null) totalMotivationBonus += staff.getMotivationBonus();
            if (staff.getTrainingBonus() != null) totalTrainingBonus += staff.getTrainingBonus();
            if (staff.getInjuryPreventionBonus() != null) totalInjuryPreventionBonus += staff.getInjuryPreventionBonus();
            if (staff.getRecoveryBonus() != null) totalRecoveryBonus += staff.getRecoveryBonus();
            if (staff.getScoutingBonus() != null) totalScoutingBonus += staff.getScoutingBonus();

            // Specific Coaching Bonuses Calculation
            switch (staff.getRole()) {
                case HEAD_COACH:
                    totalTacticalBonus += abilityMultiplier * 0.2;
                    totalAttackingBonus += abilityMultiplier * 0.1;
                    totalDefendingBonus += abilityMultiplier * 0.1;
                    break;
                case ASSISTANT_COACH:
                    totalTacticalBonus += abilityMultiplier * 0.15;
                    totalAttackingBonus += abilityMultiplier * 0.1;
                    totalDefendingBonus += abilityMultiplier * 0.1;
                    break;
                case FITNESS_COACH:
                    totalFitnessBonus += abilityMultiplier * 0.25;
                    break;
                case GOALKEEPING_COACH:
                    totalGoalkeepingBonus += abilityMultiplier * 0.4;
                    break;
                case YOUTH_COACH:
                    totalAttackingBonus += abilityMultiplier * 0.1;
                    totalDefendingBonus += abilityMultiplier * 0.1;
                    break;
                case ANALYST:
                    totalTacticalBonus += abilityMultiplier * 0.15;
                    break;
                default:
                    break;
            }
        }

        return StaffBonusesDTO.builder()
            .motivationBonus(Math.min(1.0, totalMotivationBonus))
            .trainingBonus(Math.min(1.0, totalTrainingBonus))
            .injuryPreventionBonus(Math.min(0.8, totalInjuryPreventionBonus))
            .recoveryBonus(Math.min(1.0, totalRecoveryBonus))
            .scoutingBonus(Math.min(1.0, totalScoutingBonus))
            // New specific bonuses
            .goalkeepingBonus(Math.min(1.0, totalGoalkeepingBonus))
            .defendingBonus(Math.min(1.0, totalDefendingBonus))
            .attackingBonus(Math.min(1.0, totalAttackingBonus))
            .fitnessBonus(Math.min(1.0, totalFitnessBonus))
            .tacticalBonus(Math.min(1.0, totalTacticalBonus))
            .build();
    }

    @Scheduled(initialDelayString = "${fm.scheduling.staff.initial-delay}", fixedRateString = "${fm.scheduling.staff.fixed-rate}")
    @Transactional
    public void processMonthlyStaffSalaries() {
        log.info("Starting processMonthlyStaffSalaries...");
        List<Staff> activeStaff = staffRepository.findByStatus(StaffStatus.ACTIVE);

        for (Staff staff : activeStaff) {
            if (staff.getClub() != null) {
                Club club = staff.getClub();
                BigDecimal salary = staff.getMonthlySalary();

                if (club.getFinance() != null) {
                    club.getFinance().setBalance(
                        club.getFinance().getBalance().subtract(salary)
                    );
                }
            }
        }
        log.info("Finished processMonthlyStaffSalaries.");
    }

    @Transactional
    public StaffContract renewContract(Long staffId, RenewContractRequest request) {
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        StaffContract currentContract = staffContractRepository
            .findByStaffAndStatus(staff, ContractStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Active contract not found"));

        currentContract.setStatus(ContractStatus.EXPIRED);
        currentContract.setEndDate(LocalDate.now());

        StaffContract newContract = StaffContract.builder()
            .staff(staff)
            .club(staff.getClub())
            .monthlySalary(request.getNewSalary())
            .signingBonus(request.getSigningBonus())
            .performanceBonus(request.getPerformanceBonus())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(request.getContractYears()))
            .status(ContractStatus.ACTIVE)
            .terminationFee(calculateTerminationFee(staff, request.getContractYears()))
            .build();

        staff.setMonthlySalary(request.getNewSalary());
        staff.setContractEnd(newContract.getEndDate());

        if (request.getSigningBonus().compareTo(BigDecimal.ZERO) > 0 && staff.getClub().getFinance() != null) {
             staff.getClub().getFinance().setBalance(
                staff.getClub().getFinance().getBalance().subtract(request.getSigningBonus())
             );
        }

        staffContractRepository.save(currentContract);
        staffRepository.save(staff);
        return staffContractRepository.save(newContract);
    }

    private BigDecimal calculateStaffSalary(StaffRole role, int ability, int reputation, int experience) {
        double baseSalary = (role.getMinSalary() + role.getMaxSalary()) / 2.0;
        double abilityMultiplier = 0.5 + (ability / 20.0) * 0.5;
        double reputationMultiplier = 0.7 + (reputation / 20.0) * 0.3;
        double experienceMultiplier = 0.8 + (experience / 40.0) * 0.2;

        double finalSalary = baseSalary * abilityMultiplier * reputationMultiplier * experienceMultiplier;
        return BigDecimal.valueOf(finalSalary).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTerminationFee(Staff staff, int contractYears) {
        return staff.getMonthlySalary().multiply(BigDecimal.valueOf(3L * contractYears));
    }

    public List<Staff> getClubStaff(Long clubId) {
        Club club = clubService.findById(clubId);
        return staffRepository.findByClub(club);
    }

    public Page<Staff> getAvailableStaff(StaffRole role, Pageable pageable) {
        if (role != null) {
            return staffRepository.findByClubIsNullAndStatusAndRole(StaffStatus.ACTIVE, role, pageable);
        }
        return staffRepository.findByClubIsNullAndStatus(StaffStatus.ACTIVE, pageable);
    }
}
