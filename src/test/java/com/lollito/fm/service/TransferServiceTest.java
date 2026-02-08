package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @InjectMocks
    private TransferService transferService;

    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private PlayerService playerService;
    @Mock private FinancialService financialService;
    @Mock private ClubService clubService;
    @Mock private TeamService teamService;

    @Test
    public void buyPlayer_Success() {
        Long playerId = 1L;
        Long buyerClubId = 2L;
        Long sellerClubId = 3L;

        Player player = new Player();
        player.setId(playerId);
        player.setOnSale(true);
        player.setName("John");
        player.setSurname("Doe");
        player.setStamina(50.0);

        Team sellerTeam = new Team();
        sellerTeam.setId(10L);
        Club sellerClub = new Club();
        sellerClub.setId(sellerClubId);
        sellerTeam.setClub(sellerClub);
        player.setTeam(sellerTeam);
        sellerTeam.getPlayers().add(player);

        Club buyerClub = new Club();
        buyerClub.setId(buyerClubId);
        Team buyerTeam = new Team();
        buyerTeam.setId(20L);
        buyerClub.setTeam(buyerTeam);
        buyerClub.setFinance(new Finance(new BigDecimal("100000000"))); // Rich club

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(buyerClubId)).thenReturn(buyerClub);

        transferService.buyPlayer(playerId, buyerClubId);

        // Verify transaction
        verify(financialService).processTransaction(eq(buyerClubId), any());
        verify(financialService).processTransaction(eq(sellerClubId), any());

        // Verify player transfer
        assertFalse(player.getOnSale());
        assertEquals(buyerTeam, player.getTeam());
        // Verify team lists
        assertFalse(sellerTeam.getPlayers().contains(player));
        assertTrue(buyerTeam.getPlayers().contains(player));

        verify(playerService).save(player);
    }

    @Test
    public void buyPlayer_NotOnSale() {
        Long playerId = 1L;
        Long buyerClubId = 2L;
        Player player = new Player();
        player.setOnSale(false);

        when(playerService.findOne(playerId)).thenReturn(player);

        assertThrows(RuntimeException.class, () -> transferService.buyPlayer(playerId, buyerClubId));
    }

    @Test
    public void buyPlayer_InsufficientFunds() {
        Long playerId = 1L;
        Long buyerClubId = 2L;

        Player player = new Player();
        player.setId(playerId);
        player.setOnSale(true);
        // Make player expensive
        player.setStamina(100.0);
        player.setPlaymaking(100.0);
        player.setScoring(100.0);
        player.setWinger(100.0);
        player.setGoalkeeping(100.0);
        player.setPassing(100.0);
        player.setDefending(100.0);
        player.setSetPieces(100.0);
        player.setCondition(100.0);
        // Average 100. Price = 100 * 10000 = 1,000,000.

        Team sellerTeam = new Team();
        Club sellerClub = new Club();
        sellerClub.setId(3L);
        sellerTeam.setClub(sellerClub);
        player.setTeam(sellerTeam);

        Club buyerClub = new Club();
        buyerClub.setId(buyerClubId);
        buyerClub.setFinance(new Finance(new BigDecimal("0"))); // Poor club

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(buyerClubId)).thenReturn(buyerClub);

        assertThrows(RuntimeException.class, () -> transferService.buyPlayer(playerId, buyerClubId));
    }
}
