package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.LoanAgreement;
import com.lollito.fm.model.LoanStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.TransferType;
import com.lollito.fm.model.dto.TransferOfferDTO;
import com.lollito.fm.repository.rest.LoanAgreementRepository;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @InjectMocks
    private LoanService loanService;

    @Mock private LoanAgreementRepository loanAgreementRepository;
    @Mock private FinancialService financialService;
    @Mock private PlayerService playerService;
    @Mock private PlayerHistoryService playerHistoryService;

    @Test
    public void activatePurchaseOption_Success() {
        Long loanId = 1L;
        BigDecimal optionPrice = new BigDecimal("5000000");

        Club parentClub = new Club();
        parentClub.setId(10L);
        parentClub.setName("Parent Club");

        Club loanClub = new Club();
        loanClub.setId(20L);
        loanClub.setName("Loan Club");
        Team loanTeam = new Team();
        loanTeam.setId(200L);
        loanClub.setTeam(loanTeam);

        Player player = new Player();
        player.setId(100L);
        player.setName("Player Name");
        Team originalTeam = new Team();
        originalTeam.setId(300L);
        // Assuming originalTeam is set to what player was before loan or parent club team?
        // In LoanService logic:
        // player.setTeam(loan.getLoanClub().getTeam());
        // player.setOriginalTeam(null);
        // So player currently belongs to loanClub (loanTeam) but has originalTeam set (parentClub team usually).
        // Let's assume player is currently on loan, so team is loanTeam, and originalTeam is parentTeam.
        player.setTeam(loanTeam);
        player.setOriginalTeam(originalTeam);

        LoanAgreement loan = LoanAgreement.builder()
                .id(loanId)
                .player(player)
                .parentClub(parentClub)
                .loanClub(loanClub)
                .hasOptionToBuy(true)
                .optionToBuyPrice(optionPrice)
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanAgreementRepository.findById(loanId)).thenReturn(Optional.of(loan));

        TransferOfferDTO result = loanService.activatePurchaseOption(loanId);

        // Verify financial transactions
        // Loan club pays (Expense)
        verify(financialService).processTransaction(eq(loanClub.getId()), argThat(request ->
            request.getType() == TransactionType.EXPENSE &&
            request.getCategory() == TransactionCategory.TRANSFER_FEES &&
            request.getAmount().equals(optionPrice)
        ));

        // Parent club receives (Income)
        verify(financialService).processTransaction(eq(parentClub.getId()), argThat(request ->
            request.getType() == TransactionType.INCOME &&
            request.getCategory() == TransactionCategory.TRANSFER_INCOME &&
            request.getAmount().equals(optionPrice)
        ));

        // Verify player update
        assertEquals(loanTeam, player.getTeam());
        assertEquals(null, player.getOriginalTeam());
        verify(playerService).save(player);

        // Verify history record
        verify(playerHistoryService).recordTransfer(eq(player), eq(parentClub), eq(loanClub), eq(optionPrice), eq(TransferType.PURCHASE));

        // Verify loan status update
        assertEquals(LoanStatus.COMPLETED, loan.getStatus());
        verify(loanAgreementRepository).save(loan);

        // Verify result
        assertNotNull(result);
        assertEquals(player.getId(), result.getPlayerId());
        assertEquals(loanClub.getId(), result.getBuyingClubId());
        assertEquals(parentClub.getId(), result.getSellingClubId());
        assertEquals(optionPrice, result.getOfferAmount());
        assertEquals("ACCEPTED", result.getStatus());
    }

    @Test
    public void activatePurchaseOption_LoanNotFound() {
        Long loanId = 1L;
        when(loanAgreementRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> loanService.activatePurchaseOption(loanId));
    }

    @Test
    public void activatePurchaseOption_NoOptionToBuy() {
        Long loanId = 1L;
        // Create loan with hasOptionToBuy = false
        LoanAgreement loan = LoanAgreement.builder()
                .id(loanId)
                .hasOptionToBuy(false)
                .build();

        when(loanAgreementRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> loanService.activatePurchaseOption(loanId));
    }
}
