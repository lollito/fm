package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.LoanAgreement;
import com.lollito.fm.model.LoanPerformanceReview;
import com.lollito.fm.model.LoanProposal;
import com.lollito.fm.model.LoanRecommendation;
import com.lollito.fm.model.LoanStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerSeasonStats;
import com.lollito.fm.model.ProposalStatus;
import com.lollito.fm.model.ReviewPeriod;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.TransferType;
import com.lollito.fm.model.dto.CreateLoanProposalRequest;
import com.lollito.fm.model.dto.TransferOfferDTO;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.LoanAgreementRepository;
import com.lollito.fm.repository.rest.LoanPerformanceReviewRepository;
import com.lollito.fm.repository.rest.LoanProposalRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
@Slf4j
public class LoanService {

    @Autowired private LoanAgreementRepository loanAgreementRepository;
    @Autowired private LoanProposalRepository loanProposalRepository;
    @Autowired private LoanPerformanceReviewRepository performanceReviewRepository;
    @Autowired private PlayerService playerService;
    @Autowired private ClubService clubService;
    @Autowired private ClubRepository clubRepository;
    @Autowired private FinancialService financialService;
    @Autowired private PlayerHistoryService playerHistoryService;
    @Autowired private SeasonService seasonService;
    @Autowired private NewsService newsService;

    public LoanProposal createLoanProposal(CreateLoanProposalRequest request) {
        Player player = playerService.findOne(request.getPlayerId());
        Club proposingClub = clubService.findById(request.getProposingClubId());

        Club targetClub = clubRepository.findByTeam(player.getTeam())
                .orElse(clubRepository.findByUnder18(player.getTeam()).orElse(null));

        if (targetClub == null) {
            throw new EntityNotFoundException("Club not found for player's team");
        }

        validateLoanEligibility(player, proposingClub);

        LoanProposal proposal = LoanProposal.builder()
            .player(player)
            .proposingClub(proposingClub)
            .targetClub(targetClub)
            .status(ProposalStatus.PENDING)
            .proposedStartDate(request.getStartDate())
            .proposedEndDate(request.getEndDate())
            .proposedLoanFee(request.getLoanFee())
            .proposedSalaryShare(request.getSalaryShare())
            .proposedRecallClause(request.getRecallClause())
            .proposedOptionToBuy(request.getOptionToBuy())
            .proposedOptionPrice(request.getOptionPrice())
            .proposalMessage(request.getMessage())
            .proposalDate(LocalDateTime.now())
            .expiryDate(LocalDateTime.now().plusDays(7))
            .build();

        return loanProposalRepository.save(proposal);
    }

    public LoanAgreement acceptLoanProposal(Long proposalId) {
        LoanProposal proposal = loanProposalRepository.findById(proposalId)
            .orElseThrow(() -> new EntityNotFoundException("Loan proposal not found"));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal is not pending");
        }

        LoanAgreement agreement = LoanAgreement.builder()
            .player(proposal.getPlayer())
            .parentClub(proposal.getTargetClub())
            .loanClub(proposal.getProposingClub())
            .startDate(proposal.getProposedStartDate())
            .endDate(proposal.getProposedEndDate())
            .status(LoanStatus.ACTIVE)
            .loanFee(proposal.getProposedLoanFee())
            .loanClubSalaryShare(proposal.getProposedSalaryShare())
            .parentClubSalaryShare(1.0 - (proposal.getProposedSalaryShare() != null ? proposal.getProposedSalaryShare() : 0.0))
            .hasRecallClause(proposal.getProposedRecallClause())
            .hasOptionToBuy(proposal.getProposedOptionToBuy())
            .optionToBuyPrice(proposal.getProposedOptionPrice())
            .agreementDate(LocalDateTime.now())
            .actualAppearances(0)
            .build();

        agreement = loanAgreementRepository.save(agreement);

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setResponseDate(LocalDateTime.now());
        loanProposalRepository.save(proposal);

        transferPlayerOnLoan(agreement);
        processLoanFeePayment(agreement);

        return agreement;
    }

    public void rejectLoanProposal(Long proposalId, String reason) {
        LoanProposal proposal = loanProposalRepository.findById(proposalId)
            .orElseThrow(() -> new EntityNotFoundException("Loan proposal not found"));

        proposal.setStatus(ProposalStatus.REJECTED);
        proposal.setRejectionReason(reason);
        proposal.setResponseDate(LocalDateTime.now());
        loanProposalRepository.save(proposal);
    }

    @Scheduled(initialDelayString = "${fm.scheduling.loans.initial-delay}", fixedRateString = "${fm.scheduling.loans.fixed-rate}")
    public void processMonthlyLoanReviews() {
        log.info("Starting processMonthlyLoanReviews...");
        List<LoanAgreement> activeLoans = loanAgreementRepository.findWithPlayerByStatus(LoanStatus.ACTIVE);

        if (activeLoans.isEmpty()) {
            log.info("No active loans found.");
            return;
        }

        List<Player> players = activeLoans.stream()
            .map(LoanAgreement::getPlayer)
            .collect(Collectors.toList());

        Season currentSeason = seasonService.getCurrentSeason();
        Map<Long, PlayerSeasonStats> statsMap = playerHistoryService.getSeasonStatsForPlayers(players, currentSeason);

        for (LoanAgreement loan : activeLoans) {
             PlayerSeasonStats stats = statsMap.get(loan.getPlayer().getId());
             createPerformanceReview(loan, ReviewPeriod.MONTHLY, stats);
        }
        log.info("Finished processMonthlyLoanReviews.");
    }

    public LoanPerformanceReview createPerformanceReview(LoanAgreement loan, ReviewPeriod period) {
        Player player = loan.getPlayer();
        Season currentSeason = seasonService.getCurrentSeason();
        PlayerSeasonStats stats = playerHistoryService.getPlayerSeasonStats(player.getId(), currentSeason.getId());
        return createPerformanceReview(loan, period, stats);
    }

    public void recallPlayerFromLoan(Long loanId, String reason) {
        LoanAgreement loan = loanAgreementRepository.findById(loanId)
            .orElseThrow(() -> new EntityNotFoundException("Loan agreement not found"));

        if (!Boolean.TRUE.equals(loan.getHasRecallClause())) {
             throw new IllegalStateException("Loan agreement has no recall clause");
        }

        loan.setStatus(LoanStatus.RECALLED);
        loanAgreementRepository.save(loan);

        transferPlayerBackFromLoan(loan);
        createPerformanceReview(loan, ReviewPeriod.END_SEASON);
    }

    public TransferOfferDTO activatePurchaseOption(Long loanId) {
        LoanAgreement loan = loanAgreementRepository.findById(loanId)
             .orElseThrow(() -> new EntityNotFoundException("Loan agreement not found"));

        if (!Boolean.TRUE.equals(loan.getHasOptionToBuy())) {
             throw new IllegalStateException("Loan has no purchase option");
        }

        BigDecimal price = loan.getOptionToBuyPrice();

        financialService.processTransaction(loan.getLoanClub().getId(),
            CreateTransactionRequest.builder()
                .type(TransactionType.EXPENSE)
                .category(TransactionCategory.TRANSFER_FEES)
                .amount(price)
                .description("Purchase option for " + loan.getPlayer().getName())
                .effectiveDate(LocalDate.now())
                .isRecurring(false)
                .build());

        financialService.processTransaction(loan.getParentClub().getId(),
            CreateTransactionRequest.builder()
                .type(TransactionType.INCOME)
                .category(TransactionCategory.TRANSFER_INCOME)
                .amount(price)
                .description("Sale of " + loan.getPlayer().getName() + " (Option Executed)")
                .effectiveDate(LocalDate.now())
                .isRecurring(false)
                .build());

        Player player = loan.getPlayer();
        player.setTeam(loan.getLoanClub().getTeam());
        player.setOriginalTeam(null);
        playerService.save(player);

        playerHistoryService.recordTransfer(player, loan.getParentClub(), loan.getLoanClub(), price, TransferType.PURCHASE);

        loan.setStatus(LoanStatus.COMPLETED);
        loanAgreementRepository.save(loan);

        return TransferOfferDTO.builder()
            .playerId(player.getId())
            .buyingClubId(loan.getLoanClub().getId())
            .sellingClubId(loan.getParentClub().getId())
            .offerAmount(price)
            .status("ACCEPTED")
            .build();
    }

    public List<LoanAgreement> getClubActiveLoans(Long clubId) {
        Club club = clubService.findById(clubId);
        List<LoanAgreement> parentLoans = loanAgreementRepository.findByParentClubAndStatus(club, LoanStatus.ACTIVE);
        List<LoanAgreement> loanLoans = loanAgreementRepository.findByLoanClubAndStatus(club, LoanStatus.ACTIVE);
        parentLoans.addAll(loanLoans);
        return parentLoans;
    }

    public List<LoanPerformanceReview> getPerformanceReviews(Long loanId) {
        LoanAgreement loan = loanAgreementRepository.findById(loanId)
            .orElseThrow(() -> new EntityNotFoundException("Loan not found"));
        return performanceReviewRepository.findByLoanAgreement(loan);
    }

    private void validateLoanEligibility(Player player, Club proposingClub) {
        if (player.getOriginalTeam() != null) {
            throw new IllegalStateException("Player is already on loan");
        }
        Club playerClub = clubRepository.findByTeam(player.getTeam())
                .orElse(clubRepository.findByUnder18(player.getTeam()).orElse(null));

        if (playerClub != null && playerClub.getId().equals(proposingClub.getId())) {
             throw new IllegalStateException("Cannot loan own player");
        }
    }

    private void transferPlayerOnLoan(LoanAgreement agreement) {
        Player player = agreement.getPlayer();
        player.setOriginalTeam(player.getTeam());
        player.setTeam(agreement.getLoanClub().getTeam());
        playerService.save(player);

        playerHistoryService.recordTransfer(player, agreement.getParentClub(), agreement.getLoanClub(), BigDecimal.ZERO, TransferType.LOAN);
    }

    private void transferPlayerBackFromLoan(LoanAgreement agreement) {
         Player player = agreement.getPlayer();
         if (player.getOriginalTeam() != null) {
             player.setTeam(player.getOriginalTeam());
             player.setOriginalTeam(null);
             playerService.save(player);

             playerHistoryService.recordTransfer(player, agreement.getLoanClub(), agreement.getParentClub(), BigDecimal.ZERO, TransferType.LOAN);
         }
    }

    private void processLoanFeePayment(LoanAgreement agreement) {
        if (agreement.getLoanFee() != null && agreement.getLoanFee().compareTo(BigDecimal.ZERO) > 0) {
             financialService.processTransaction(agreement.getLoanClub().getId(),
                CreateTransactionRequest.builder()
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.LOAN_PAYMENTS)
                    .amount(agreement.getLoanFee())
                    .description("Loan fee for " + agreement.getPlayer().getName())
                    .effectiveDate(LocalDate.now())
                    .isRecurring(false)
                    .build());

             financialService.processTransaction(agreement.getParentClub().getId(),
                CreateTransactionRequest.builder()
                    .type(TransactionType.INCOME)
                    .category(TransactionCategory.LOAN_INCOME)
                    .amount(agreement.getLoanFee())
                    .description("Loan fee received for " + agreement.getPlayer().getName())
                    .effectiveDate(LocalDate.now())
                    .isRecurring(false)
                    .build());
        }
    }

    private LoanPerformanceReview createPerformanceReview(LoanAgreement loan, ReviewPeriod period, PlayerSeasonStats stats) {
        Integer matches = stats != null ? stats.getMatchesPlayed() : 0;
        Integer goals = stats != null ? stats.getGoals() : 0;

        LoanPerformanceReview review = LoanPerformanceReview.builder()
            .loanAgreement(loan)
            .reviewDate(LocalDate.now())
            .period(period)
            .matchesPlayed(matches)
            .goals(goals)
            .assists(stats != null ? stats.getAssists() : 0)
            .averageRating(stats != null ? stats.getAverageRating() : 0.0)
            .yellowCards(stats != null ? stats.getYellowCards() : 0)
            .redCards(stats != null ? stats.getRedCards() : 0)
            .skillImprovement(0.0)
            .targetsMet(false)
            .recommendation(generateLoanRecommendation(loan, stats))
            .build();

        review = performanceReviewRepository.save(review);

        loan.setActualAppearances(matches);
        loanAgreementRepository.save(loan);

        return review;
    }

    private LoanRecommendation generateLoanRecommendation(LoanAgreement loan, PlayerSeasonStats stats) {
        if (stats == null) return LoanRecommendation.CONTINUE;
        if (stats.getAverageRating() > 7.0 && stats.getMatchesPlayed() > 5) return LoanRecommendation.CONTINUE;
        return LoanRecommendation.RECALL;
    }
}
