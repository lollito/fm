package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.FacilityUpgrade;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.UpgradeStatus;
import com.lollito.fm.model.UpgradeType;
import com.lollito.fm.model.dto.FacilityUpgradeDTO;
import com.lollito.fm.model.dto.StartUpgradeRequest;
import com.lollito.fm.repository.rest.FacilityUpgradeRepository;
import com.lollito.fm.repository.rest.StadiumRepository;

public class InfrastructureServiceTest {

    @InjectMocks
    private InfrastructureService infrastructureService;

    @Mock
    private ClubService clubService;

    @Mock
    private FinancialService financialService;

    @Mock
    private FacilityUpgradeRepository facilityUpgradeRepository;

    @Mock
    private StadiumRepository stadiumRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStartUpgrade_StadiumCapacity() {
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        Finance finance = new Finance();
        finance.setBalance(new BigDecimal("10000000"));
        club.setFinance(finance);

        StartUpgradeRequest request = new StartUpgradeRequest();
        request.setFacilityType(FacilityType.STADIUM);
        request.setFacilityId(10L);
        request.setUpgradeType(UpgradeType.CAPACITY_EXPANSION);
        request.setUpgradeName("Expansion");
        request.setCost(new BigDecimal("1000000"));
        request.setDurationDays(30);

        when(clubService.findById(clubId)).thenReturn(club);
        when(facilityUpgradeRepository.save(any(FacilityUpgrade.class))).thenAnswer(i -> {
            FacilityUpgrade u = i.getArgument(0);
            u.setId(100L);
            return u;
        });

        FacilityUpgradeDTO result = infrastructureService.startUpgrade(clubId, request);

        assertNotNull(result);
        assertEquals(UpgradeStatus.IN_PROGRESS, result.getStatus());
        verify(financialService).processTransaction(any(), any());
        verify(facilityUpgradeRepository).save(any(FacilityUpgrade.class));
    }

    @Test
    public void testCompleteUpgrade_Stadium() {
        Long upgradeId = 5L;
        Long facilityId = 10L;
        FacilityUpgrade upgrade = new FacilityUpgrade();
        upgrade.setId(upgradeId);
        upgrade.setFacilityType(FacilityType.STADIUM);
        upgrade.setFacilityId(facilityId);
        upgrade.setUpgradeType(UpgradeType.CAPACITY_EXPANSION);
        upgrade.setStatus(UpgradeStatus.IN_PROGRESS);
        upgrade.setBonusEffects("5000");
        upgrade.setCost(BigDecimal.ZERO); // needed for upgrade value add

        Stadium stadium = new Stadium();
        stadium.setId(facilityId);
        stadium.setCapacity(10000);
        stadium.setMaintenanceCost(BigDecimal.ZERO);
        stadium.setUpgradeValue(BigDecimal.ZERO);
        stadium.setPitchQuality(5);
        stadium.setFacilitiesQuality(5);

        when(facilityUpgradeRepository.findById(upgradeId)).thenReturn(Optional.of(upgrade));
        when(stadiumRepository.findById(facilityId)).thenReturn(Optional.of(stadium));

        infrastructureService.completeUpgrade(upgradeId);

        assertEquals(UpgradeStatus.COMPLETED, upgrade.getStatus());
        assertEquals(15000, stadium.getCapacity());
        verify(stadiumRepository).save(stadium);
    }
}
