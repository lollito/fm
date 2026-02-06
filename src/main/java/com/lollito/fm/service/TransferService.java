package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.rest.CreateTransactionRequest;

@Service
public class TransferService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private PlayerService playerService;
    @Autowired private FinancialService financialService;
    @Autowired private ClubService clubService;
    @Autowired private TeamService teamService;

    @Transactional
    public void buyPlayer(Long playerId, Long buyerClubId) {
        Player player = playerService.findOne(playerId);
        if (!Boolean.TRUE.equals(player.getOnSale())) {
            throw new RuntimeException("Player is not on sale");
        }

        Club buyerClub = clubService.findById(buyerClubId);
        Club sellerClub = player.getTeam().getClub();

        if (buyerClub.getId().equals(sellerClub.getId())) {
             throw new RuntimeException("You cannot buy your own player");
        }

        BigDecimal price = player.getMarketValue();

        // Check funds
        if (buyerClub.getFinance().getBalance().compareTo(price) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // Process Buyer Transaction
        financialService.processTransaction(buyerClub.getId(), CreateTransactionRequest.builder()
                .type(TransactionType.EXPENSE)
                .category(TransactionCategory.TRANSFER_FEES)
                .amount(price)
                .description("Bought player " + player.getName() + " " + player.getSurname())
                .reference("TRANSFER_OUT_" + player.getId())
                .effectiveDate(LocalDate.now())
                .isRecurring(false)
                .build());

        // Process Seller Transaction
        financialService.processTransaction(sellerClub.getId(), CreateTransactionRequest.builder()
                .type(TransactionType.INCOME)
                .category(TransactionCategory.TRANSFER_INCOME)
                .amount(price)
                .description("Sold player " + player.getName() + " " + player.getSurname())
                .reference("TRANSFER_IN_" + player.getId())
                .effectiveDate(LocalDate.now())
                .isRecurring(false)
                .build());

        // Transfer Player
        player.getTeam().removePlayer(player);
        buyerClub.getTeam().addPlayer(player);
        player.setOnSale(false);

        playerService.save(player);
    }
}
