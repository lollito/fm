package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.dto.request.HireStaffRequest;
import com.lollito.fm.dto.StaffBonusesDTO;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.ContractStatus;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.Staff;
import com.lollito.fm.model.StaffContract;
import com.lollito.fm.model.StaffRole;
import com.lollito.fm.model.StaffStatus;
import com.lollito.fm.repository.StaffContractRepository;
import com.lollito.fm.repository.StaffRepository;

@ExtendWith(MockitoExtension.class)
public class StaffServiceTest {

    @InjectMocks
    private StaffService staffService;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StaffContractRepository staffContractRepository;

    @Mock
    private ClubService clubService;

    @Mock
    private NameService nameService;

    @Mock
    private CountryService countryService;

    private Club club;
    private Staff staff;

    @BeforeEach
    public void setup() {
        club = new Club();
        club.setId(1L);
        Finance finance = new Finance();
        finance.setBalance(new BigDecimal("1000000"));
        club.setFinance(finance);

        staff = new Staff();
        staff.setId(1L);
        staff.setName("John");
        staff.setSurname("Doe");
        staff.setRole(StaffRole.HEAD_COACH);
        staff.setMonthlySalary(new BigDecimal("10000"));
        staff.setStatus(StaffStatus.ACTIVE);
        staff.setAbility(15);
    }

    @Test
    public void testGenerateAvailableStaff() {
        when(countryService.findAll()).thenReturn(List.of(new Country()));
        when(nameService.getNames()).thenReturn(List.of("John"));
        when(nameService.getSurnames()).thenReturn(List.of("Doe"));
        when(staffRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);

        List<Staff> generated = staffService.generateAvailableStaff(StaffRole.HEAD_COACH, 5);

        assertEquals(5, generated.size());
        assertEquals(StaffRole.HEAD_COACH, generated.get(0).getRole());
    }

    @Test
    public void testHireStaff() {
        when(clubService.findById(1L)).thenReturn(club);
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffContractRepository.save(any(StaffContract.class))).thenAnswer(i -> i.getArguments()[0]);

        HireStaffRequest request = new HireStaffRequest();
        request.setClubId(1L);
        request.setStaffId(1L);
        request.setContractYears(2);
        request.setSigningBonus(new BigDecimal("5000"));
        request.setPerformanceBonus(new BigDecimal("1000"));

        StaffContract contract = staffService.hireStaff(1L, 1L, request);

        assertNotNull(contract);
        assertEquals(club, contract.getClub());
        assertEquals(staff, contract.getStaff());
        assertEquals(ContractStatus.ACTIVE, contract.getStatus());
        assertEquals(new BigDecimal("10000"), contract.getMonthlySalary());

        // Balance reduced by signing bonus
        assertEquals(new BigDecimal("995000"), club.getFinance().getBalance());
    }

    @Test
    public void testFireStaff() {
        staff.setClub(club);
        StaffContract contract = new StaffContract();
        contract.setStaff(staff);
        contract.setClub(club);
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setTerminationFee(new BigDecimal("30000"));
        contract.setMonthlySalary(new BigDecimal("10000"));

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffContractRepository.findByStaffAndStatus(staff, ContractStatus.ACTIVE)).thenReturn(Optional.of(contract));

        staffService.fireStaff(1L, "Poor performance");

        assertEquals(StaffStatus.TERMINATED, staff.getStatus());
        assertEquals(ContractStatus.TERMINATED, contract.getStatus());

        // Balance reduced by termination fee
        assertEquals(new BigDecimal("970000"), club.getFinance().getBalance());
    }

    @Test
    public void testCalculateClubStaffBonuses() {
        Staff headCoach = new Staff();
        headCoach.setRole(StaffRole.HEAD_COACH);
        headCoach.setAbility(15);
        headCoach.setStatus(StaffStatus.ACTIVE);
        headCoach.setTrainingBonus(0.1);

        Staff fitnessCoach = new Staff();
        fitnessCoach.setRole(StaffRole.FITNESS_COACH);
        fitnessCoach.setAbility(20);
        fitnessCoach.setStatus(StaffStatus.ACTIVE);
        fitnessCoach.setTrainingBonus(0.2);

        Staff goalkeepingCoach = new Staff();
        goalkeepingCoach.setRole(StaffRole.GOALKEEPING_COACH);
        goalkeepingCoach.setAbility(10);
        goalkeepingCoach.setStatus(StaffStatus.ACTIVE);
        goalkeepingCoach.setTrainingBonus(0.1);

        club.setStaff(List.of(headCoach, fitnessCoach, goalkeepingCoach));

        when(clubService.findById(1L)).thenReturn(club);

        StaffBonusesDTO bonuses = staffService.calculateClubStaffBonuses(1L);

        // Head Coach: Ability 15. Multiplier = 0.75.
        // Tactical = 0.75 * 0.2 = 0.15
        // Attacking = 0.75 * 0.1 = 0.075
        // Defending = 0.75 * 0.1 = 0.075

        // Fitness Coach: Ability 20. Multiplier = 1.0.
        // Fitness = 1.0 * 0.25 = 0.25

        // GK Coach: Ability 10. Multiplier = 0.5.
        // Goalkeeping = 0.5 * 0.4 = 0.20

        assertEquals(0.15, bonuses.getTacticalBonus(), 0.0001, "Tactical bonus mismatch");
        assertEquals(0.075, bonuses.getAttackingBonus(), 0.0001, "Attacking bonus mismatch");
        assertEquals(0.075, bonuses.getDefendingBonus(), 0.0001, "Defending bonus mismatch");
        assertEquals(0.25, bonuses.getFitnessBonus(), 0.0001, "Fitness bonus mismatch");
        assertEquals(0.20, bonuses.getGoalkeepingBonus(), 0.0001, "Goalkeeping bonus mismatch");

        // Also check legacy fields
        // TrainingBonus: 0.1 + 0.2 + 0.1 = 0.4
        assertEquals(0.4, bonuses.getTrainingBonus(), 0.0001, "Training bonus mismatch");
    }
}
