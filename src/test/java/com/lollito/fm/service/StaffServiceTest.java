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
}
