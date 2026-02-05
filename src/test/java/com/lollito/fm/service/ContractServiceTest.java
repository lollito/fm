package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Contract;
import com.lollito.fm.model.ContractNegotiation;
import com.lollito.fm.model.ContractStatus;
import com.lollito.fm.model.NegotiationOffer;
import com.lollito.fm.model.NegotiationStatus;
import com.lollito.fm.model.NegotiationType;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.dto.ContractOfferRequest;
import com.lollito.fm.repository.rest.ContractNegotiationRepository;
import com.lollito.fm.repository.rest.ContractRepository;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractNegotiationRepository negotiationRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private ClubService clubService;

    @Mock
    private FinancialService financialService;

    @Mock
    private NewsService newsService;

    @InjectMocks
    private ContractService contractService;

    @Test
    void testNegotiationStart() {
        Player player = new Player();
        player.setId(1L);
        player.setBirth(LocalDate.now().minusYears(25));
        Club club = new Club();
        club.setId(1L);
        ContractOfferRequest offer = ContractOfferRequest.builder()
            .weeklySalary(BigDecimal.valueOf(1000))
            .signingBonus(BigDecimal.valueOf(5000))
            .loyaltyBonus(BigDecimal.valueOf(1000))
            .contractYears(3)
            .releaseClause(BigDecimal.valueOf(100000))
            .build();

        when(playerService.findOne(1L)).thenReturn(player);
        when(clubService.findById(1L)).thenReturn(club);
        when(negotiationRepository.findByPlayerAndClubAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        when(negotiationRepository.save(any(ContractNegotiation.class))).thenAnswer(i -> i.getArguments()[0]);

        ContractNegotiation negotiation = contractService.startNegotiation(
            player.getId(), club.getId(), NegotiationType.NEW_CONTRACT, offer);

        assertThat(negotiation.getPlayer()).isEqualTo(player);
        assertThat(negotiation.getClub()).isEqualTo(club);
        assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.IN_PROGRESS);
        assertThat(negotiation.getOffers()).hasSize(1);
    }

    @Test
    void testAcceptOffer() {
        Player player = new Player();
        player.setId(1L);
        Club club = new Club();
        club.setId(1L);

        ContractNegotiation negotiation = ContractNegotiation.builder()
            .id(1L)
            .player(player)
            .club(club)
            .status(NegotiationStatus.IN_PROGRESS)
            .offeredWeeklySalary(BigDecimal.valueOf(2000))
            .offeredContractYears(3)
            .roundsOfNegotiation(1)
            .build();

        when(negotiationRepository.findById(1L)).thenReturn(Optional.of(negotiation));
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> i.getArguments()[0]);

        Contract contract = contractService.acceptOffer(negotiation.getId());

        assertThat(contract.getPlayer()).isEqualTo(player);
        assertThat(contract.getWeeklySalary()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
        verify(negotiationRepository).save(negotiation);
        assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.ACCEPTED);
    }
}
