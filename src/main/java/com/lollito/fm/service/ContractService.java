package com.lollito.fm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.BonusType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Contract;
import com.lollito.fm.model.ContractNegotiation;
import com.lollito.fm.model.ContractStatus;
import com.lollito.fm.model.NegotiationOffer;
import com.lollito.fm.model.NegotiationStatus;
import com.lollito.fm.model.NegotiationType;
import com.lollito.fm.model.OfferSide;
import com.lollito.fm.model.PerformanceBonus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.PlayerSeasonStats;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.dto.ContractDemands;
import com.lollito.fm.model.dto.ContractOfferRequest;
import com.lollito.fm.model.dto.OfferResponse;
import com.lollito.fm.model.dto.TransferOfferDTO;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.repository.rest.ContractNegotiationRepository;
import com.lollito.fm.repository.rest.ContractRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractNegotiationRepository negotiationRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ClubService clubService;

    @Autowired
    private FinancialService financialService;

    @Autowired
    private NewsService newsService;

    public ContractNegotiation startNegotiation(Long playerId, Long clubId,
                                              NegotiationType type, ContractOfferRequest initialOffer) {
        Player player = playerService.findOne(playerId);
        Club club = clubService.findById(clubId);

        Optional<ContractNegotiation> existingNegotiation = negotiationRepository
            .findByPlayerAndClubAndStatus(player, club, NegotiationStatus.IN_PROGRESS);

        if (existingNegotiation.isPresent()) {
            throw new IllegalStateException("Negotiation already in progress");
        }

        ContractDemands playerDemands = calculatePlayerDemands(player, type);

        ContractNegotiation negotiation = ContractNegotiation.builder()
            .player(player)
            .club(club)
            .currentContract(player.getCurrentContract())
            .type(type)
            .status(NegotiationStatus.IN_PROGRESS)
            .offeredWeeklySalary(initialOffer.getWeeklySalary())
            .offeredSigningBonus(initialOffer.getSigningBonus())
            .offeredLoyaltyBonus(initialOffer.getLoyaltyBonus())
            .offeredContractYears(initialOffer.getContractYears())
            .offeredReleaseClause(initialOffer.getReleaseClause())
            .demandedWeeklySalary(playerDemands.getWeeklySalary())
            .demandedSigningBonus(playerDemands.getSigningBonus())
            .demandedLoyaltyBonus(playerDemands.getLoyaltyBonus())
            .demandedContractYears(playerDemands.getContractYears())
            .demandedReleaseClause(playerDemands.getReleaseClause())
            .startDate(LocalDateTime.now())
            .expiryDate(LocalDateTime.now().plusDays(7))
            .roundsOfNegotiation(0)
            .build();

        negotiation = negotiationRepository.save(negotiation);

        createNegotiationOffer(negotiation, OfferSide.CLUB, initialOffer);

        return negotiation;
    }

    public NegotiationOffer makeCounterOffer(Long negotiationId, OfferSide offerSide,
                                           ContractOfferRequest offer) {
        ContractNegotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new EntityNotFoundException("Negotiation not found"));

        if (negotiation.getStatus() != NegotiationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Negotiation is not active");
        }

        if (LocalDateTime.now().isAfter(negotiation.getExpiryDate())) {
            negotiation.setStatus(NegotiationStatus.EXPIRED);
            negotiationRepository.save(negotiation);
            throw new IllegalStateException("Negotiation has expired");
        }

        if (offerSide == OfferSide.CLUB) {
            negotiation.setOfferedWeeklySalary(offer.getWeeklySalary());
            negotiation.setOfferedSigningBonus(offer.getSigningBonus());
            negotiation.setOfferedLoyaltyBonus(offer.getLoyaltyBonus());
            negotiation.setOfferedContractYears(offer.getContractYears());
            negotiation.setOfferedReleaseClause(offer.getReleaseClause());
        } else {
            negotiation.setDemandedWeeklySalary(offer.getWeeklySalary());
            negotiation.setDemandedSigningBonus(offer.getSigningBonus());
            negotiation.setDemandedLoyaltyBonus(offer.getLoyaltyBonus());
            negotiation.setDemandedContractYears(offer.getContractYears());
            negotiation.setDemandedReleaseClause(offer.getReleaseClause());
        }

        negotiation.setRoundsOfNegotiation(negotiation.getRoundsOfNegotiation() + 1);
        negotiation.setLastOfferDate(LocalDateTime.now());

        negotiationRepository.save(negotiation);

        NegotiationOffer negotiationOffer = createNegotiationOffer(negotiation, offerSide, offer);

        processOfferResponse(negotiation, negotiationOffer);

        return negotiationOffer;
    }

    public Contract acceptOffer(Long negotiationId) {
        ContractNegotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new EntityNotFoundException("Negotiation not found"));

        if (negotiation.getStatus() != NegotiationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Negotiation is not active");
        }

        Contract contract = Contract.builder()
            .player(negotiation.getPlayer())
            .club(negotiation.getClub())
            .weeklySalary(negotiation.getOfferedWeeklySalary())
            .signingBonus(negotiation.getOfferedSigningBonus())
            .loyaltyBonus(negotiation.getOfferedLoyaltyBonus())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(negotiation.getOfferedContractYears()))
            .releaseClause(negotiation.getOfferedReleaseClause())
            .hasReleaseClause(negotiation.getOfferedReleaseClause() != null)
            .status(ContractStatus.ACTIVE)
            .lastNegotiationDate(LocalDateTime.now())
            .negotiationAttempts(negotiation.getRoundsOfNegotiation())
            .build();

        contract = contractRepository.save(contract);

        if (negotiation.getCurrentContract() != null) {
            Contract oldContract = negotiation.getCurrentContract();
            oldContract.setStatus(ContractStatus.TERMINATED);
            contractRepository.save(oldContract);
        }

        Player player = negotiation.getPlayer();
        player.setCurrentContract(contract);
        player.setSalary(contract.getWeeklySalary().multiply(BigDecimal.valueOf(52)));
        playerService.save(player);

        negotiation.setStatus(NegotiationStatus.ACCEPTED);
        negotiationRepository.save(negotiation);

        if (contract.getSigningBonus() != null && contract.getSigningBonus().compareTo(BigDecimal.ZERO) > 0) {
            processSigningBonus(contract);
        }

        createDefaultContractClauses(contract);

        return contract;
    }

    public void rejectOffer(Long negotiationId, String reason) {
        ContractNegotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new EntityNotFoundException("Negotiation not found"));

        negotiation.setStatus(NegotiationStatus.REJECTED);
        negotiation.setRejectionReason(reason);
        negotiationRepository.save(negotiation);
    }

    public Contract getPlayerCurrentContract(Long playerId) {
        Player player = playerService.findOne(playerId);
        return player.getCurrentContract();
    }

    public List<ContractNegotiation> getClubNegotiations(Long clubId, NegotiationStatus status) {
        Club club = clubService.findById(clubId);
        if (status != null) {
            return negotiationRepository.findByClubAndStatus(club, status);
        }
        return negotiationRepository.findByClub(club);
    }

    public List<Contract> getExpiringContracts(int monthsAhead) {
        return contractRepository.findByStatusAndEndDateBefore(ContractStatus.ACTIVE, LocalDate.now().plusMonths(monthsAhead));
    }

    private ContractDemands calculatePlayerDemands(Player player, NegotiationType type) {
        BigDecimal baseWeeklySalary = calculateBaseWeeklySalary(player);

        double multiplier = switch (type) {
            case NEW_CONTRACT -> 1.0;
            case RENEWAL -> 1.1;
            case RENEGOTIATION -> 1.2;
        };

        BigDecimal demandedSalary = baseWeeklySalary.multiply(BigDecimal.valueOf(multiplier));

        BigDecimal signingBonus = demandedSalary.multiply(BigDecimal.valueOf(10));
        BigDecimal loyaltyBonus = demandedSalary.multiply(BigDecimal.valueOf(5));
        Integer contractYears = calculateDesiredContractLength(player);
        BigDecimal releaseClause = calculateDesiredReleaseClause(player, demandedSalary);

        return ContractDemands.builder()
            .weeklySalary(demandedSalary)
            .signingBonus(signingBonus)
            .loyaltyBonus(loyaltyBonus)
            .contractYears(contractYears)
            .releaseClause(releaseClause)
            .build();
    }

    private BigDecimal calculateBaseWeeklySalary(Player player) {
        Integer playerAverage = player.getAverage();
        int age = player.getAge();
        PlayerRole position = player.getRole();

        double baseSalary = playerAverage * 1000;

        if (age < 23) baseSalary *= 0.8;
        else if (age > 30) baseSalary *= 0.9;

        if (position != null) {
            switch (position) {
                case GOALKEEPER -> baseSalary *= 0.9;
                case DEFENDER -> baseSalary *= 0.95;
                case MIDFIELDER -> baseSalary *= 1.0;
                case WING -> baseSalary *= 1.05;
                case FORWARD -> baseSalary *= 1.1;
                default -> {}
            }
        }

        return BigDecimal.valueOf(baseSalary).setScale(0, RoundingMode.HALF_UP);
    }

    private Integer calculateDesiredContractLength(Player player) {
        int age = player.getAge();

        if (age < 25) return 4;
        else if (age < 30) return 3;
        else if (age < 35) return 2;
        else return 1;
    }

    private BigDecimal calculateDesiredReleaseClause(Player player, BigDecimal weeklySalary) {
        return weeklySalary.multiply(BigDecimal.valueOf(52)).multiply(BigDecimal.valueOf(2.5));
    }

    private NegotiationOffer createNegotiationOffer(ContractNegotiation negotiation, OfferSide offerSide, ContractOfferRequest offer) {
        NegotiationOffer negotiationOffer = NegotiationOffer.builder()
            .negotiation(negotiation)
            .offerSide(offerSide)
            .weeklySalary(offer.getWeeklySalary())
            .signingBonus(offer.getSigningBonus())
            .loyaltyBonus(offer.getLoyaltyBonus())
            .contractYears(offer.getContractYears())
            .releaseClause(offer.getReleaseClause())
            .offerDate(LocalDateTime.now())
            .status(com.lollito.fm.model.OfferStatus.PENDING)
            .build();

        negotiation.getOffers().add(negotiationOffer);
        negotiationRepository.save(negotiation);

        return negotiation.getOffers().get(negotiation.getOffers().size() - 1);
    }

    private void processOfferResponse(ContractNegotiation negotiation, NegotiationOffer offer) {
        if (offer.getOfferSide() == OfferSide.CLUB) {
            OfferResponse response = calculatePlayerResponse(negotiation, offer);

            switch (response.getDecision()) {
                case ACCEPT -> {
                    offer.setStatus(com.lollito.fm.model.OfferStatus.ACCEPTED);
                    negotiation.setStatus(NegotiationStatus.ACCEPTED);
                }
                case REJECT -> {
                    offer.setStatus(com.lollito.fm.model.OfferStatus.REJECTED);
                    negotiation.setStatus(NegotiationStatus.REJECTED);
                    negotiation.setRejectionReason(response.getReason());
                }
                case COUNTER -> {
                    offer.setStatus(com.lollito.fm.model.OfferStatus.COUNTERED);
                    scheduleAICounterOffer(negotiation);
                }
            }
        }
        negotiationRepository.save(negotiation);
    }

    private OfferResponse calculatePlayerResponse(ContractNegotiation negotiation, NegotiationOffer offer) {
        BigDecimal demanded = negotiation.getDemandedWeeklySalary();
        BigDecimal offered = offer.getWeeklySalary();

        if (demanded == null) demanded = BigDecimal.ZERO;
        if (offered == null) offered = BigDecimal.ZERO;

        if (offered.compareTo(demanded.multiply(BigDecimal.valueOf(0.9))) >= 0) {
            return OfferResponse.builder().decision(OfferResponse.OfferDecision.ACCEPT).build();
        } else if (offered.compareTo(demanded.multiply(BigDecimal.valueOf(0.7))) >= 0) {
            return OfferResponse.builder().decision(OfferResponse.OfferDecision.COUNTER).reason("Offer too low").build();
        } else {
            return OfferResponse.builder().decision(OfferResponse.OfferDecision.REJECT).reason("Insulting offer").build();
        }
    }

    private void scheduleAICounterOffer(ContractNegotiation negotiation) {
        BigDecimal currentDemanded = negotiation.getDemandedWeeklySalary();
        BigDecimal newDemanded = currentDemanded.multiply(BigDecimal.valueOf(0.95));
        negotiation.setDemandedWeeklySalary(newDemanded);
        negotiationRepository.save(negotiation);

        ContractOfferRequest counter = ContractOfferRequest.builder()
            .weeklySalary(newDemanded)
            .signingBonus(negotiation.getDemandedSigningBonus())
            .loyaltyBonus(negotiation.getDemandedLoyaltyBonus())
            .contractYears(negotiation.getDemandedContractYears())
            .releaseClause(negotiation.getDemandedReleaseClause())
            .build();

        createNegotiationOffer(negotiation, OfferSide.PLAYER, counter);
    }

    public void addPerformanceBonus(Long contractId, BonusType type, Integer targetValue,
                                  BigDecimal bonusAmount, String description) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        PerformanceBonus bonus = PerformanceBonus.builder()
            .contract(contract)
            .type(type)
            .description(description)
            .targetValue(targetValue)
            .bonusAmount(bonusAmount)
            .isAchieved(false)
            .build();

        contract.getPerformanceBonuses().add(bonus);
        contractRepository.save(contract);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void processPerformanceBonuses() {
        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);

        for (Contract contract : activeContracts) {
            for (PerformanceBonus bonus : contract.getPerformanceBonuses()) {
                if (!Boolean.TRUE.equals(bonus.getIsAchieved()) && checkBonusCondition(bonus)) {
                    bonus.setIsAchieved(true);
                    bonus.setAchievedDate(LocalDate.now());

                    processPerformanceBonus(bonus);
                    createBonusAchievementNews(bonus);
                }
            }
            contractRepository.save(contract);
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void processContractExpiries() {
        List<Contract> expiringContracts = contractRepository
            .findByStatusAndEndDateBefore(ContractStatus.ACTIVE, LocalDate.now());

        for (Contract contract : expiringContracts) {
            if (shouldAutoExtend(contract)) {
                extendContract(contract);
            } else {
                contract.setStatus(ContractStatus.EXPIRED);
                contractRepository.save(contract);

                Player player = contract.getPlayer();
                player.setCurrentContract(null);
                player.setTeam(null);
                playerService.save(player);

                createContractExpiryNews(contract);
            }
        }
    }

    private boolean checkBonusCondition(PerformanceBonus bonus) {
        Player player = bonus.getContract().getPlayer();
        PlayerSeasonStats currentStats = player.getCurrentSeasonStats();

        if (currentStats == null) return false;

        return switch (bonus.getType()) {
            case GOALS -> currentStats.getGoals() >= bonus.getTargetValue();
            case ASSISTS -> currentStats.getAssists() >= bonus.getTargetValue();
            case APPEARANCES -> currentStats.getMatchesPlayed() >= bonus.getTargetValue();
            case CLEAN_SHEETS -> currentStats.getCleanSheets() >= bonus.getTargetValue();
            default -> false;
        };
    }

    private void processPerformanceBonus(PerformanceBonus bonus) {
         CreateTransactionRequest request = CreateTransactionRequest.builder()
            .type(TransactionType.EXPENSE)
            .category(TransactionCategory.PLAYER_SALARIES)
            .amount(bonus.getBonusAmount())
            .description("Performance bonus (" + bonus.getType() + ") for " + bonus.getContract().getPlayer().getName())
            .effectiveDate(LocalDate.now())
            .isRecurring(false)
            .build();

        financialService.processTransaction(bonus.getContract().getClub().getId(), request);
    }

    private void createBonusAchievementNews(PerformanceBonus bonus) {
        String message = bonus.getContract().getPlayer().getName() + " has achieved a performance bonus: " + bonus.getDescription();
        newsService.save(new com.lollito.fm.model.News(message, LocalDateTime.now()));
    }

    private boolean shouldAutoExtend(Contract contract) {
        return false;
    }

    private void extendContract(Contract contract) {
        contract.setEndDate(contract.getEndDate().plusYears(1));
        contractRepository.save(contract);
    }

    private void createContractExpiryNews(Contract contract) {
        String message = "Contract expired for " + contract.getPlayer().getName() + " at " + contract.getClub().getName();
        newsService.save(new com.lollito.fm.model.News(message, LocalDateTime.now()));
    }

    private void processSigningBonus(Contract contract) {
        if (contract.getSigningBonus() == null || contract.getSigningBonus().compareTo(BigDecimal.ZERO) <= 0) return;

        CreateTransactionRequest request = CreateTransactionRequest.builder()
            .type(TransactionType.EXPENSE)
            .category(TransactionCategory.SIGNING_BONUS)
            .amount(contract.getSigningBonus())
            .description("Signing bonus for " + contract.getPlayer().getName())
            .effectiveDate(LocalDate.now())
            .isRecurring(false)
            .build();

        financialService.processTransaction(contract.getClub().getId(), request);
    }

    private void createDefaultContractClauses(Contract contract) {
        // Implementation for default clauses (can be empty for now)
    }

    public TransferOfferDTO triggerReleaseClause(Long playerId, Long buyingClubId) {
        Player player = playerService.findOne(playerId);
        Club buyingClub = clubService.findById(buyingClubId);

        Contract contract = player.getCurrentContract();
        if (contract == null || !contract.getHasReleaseClause()) {
            throw new IllegalStateException("Player has no release clause");
        }

        BigDecimal releaseClauseAmount = contract.getReleaseClause();

        if (buyingClub.getFinance().getBalance().compareTo(releaseClauseAmount) < 0) {
             throw new RuntimeException("Club cannot afford release clause");
        }

        TransferOfferDTO offer = TransferOfferDTO.builder()
            .playerId(player.getId())
            .buyingClubId(buyingClub.getId())
            .sellingClubId(contract.getClub().getId())
            .offerAmount(releaseClauseAmount)
            .isReleaseClause(true)
            .status("ACCEPTED")
            .offerDate(LocalDateTime.now())
            .build();

        return offer;
    }
}
