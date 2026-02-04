# Contract Negotiation System Implementation

## Overview
Implement a comprehensive contract negotiation system with detailed contract terms including duration, release clauses, signing bonuses, performance bonuses, and salary negotiations.

## Technical Requirements

### Database Schema Changes

#### New Entity: Contract
```java
@Entity
@Table(name = "contract")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    private BigDecimal weeklySalary;
    private BigDecimal signingBonus;
    private BigDecimal loyaltyBonus;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate endDate;
    
    private BigDecimal releaseClause;
    private Boolean hasReleaseClause;
    
    @Enumerated(EnumType.STRING)
    private ContractStatus status; // ACTIVE, EXPIRED, TERMINATED, NEGOTIATING
    
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<ContractClause> clauses = new ArrayList<>();
    
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<PerformanceBonus> performanceBonuses = new ArrayList<>();
    
    private Integer automaticExtensionYears; // Auto-extend if conditions met
    private String automaticExtensionConditions;
    
    private LocalDateTime lastNegotiationDate;
    private Integer negotiationAttempts;
}
```

#### New Entity: ContractClause
```java
@Entity
@Table(name = "contract_clause")
public class ContractClause {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;
    
    @Enumerated(EnumType.STRING)
    private ClauseType type;
    
    private String description;
    private String conditions;
    private BigDecimal value;
    private Boolean isActive;
}
```

#### New Entity: PerformanceBonus
```java
@Entity
@Table(name = "performance_bonus")
public class PerformanceBonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;
    
    @Enumerated(EnumType.STRING)
    private BonusType type;
    
    private String description;
    private String triggerCondition; // JSON condition
    private BigDecimal bonusAmount;
    private Integer targetValue; // Goals, assists, matches, etc.
    private Boolean isAchieved;
    private LocalDate achievedDate;
}
```

#### New Entity: ContractNegotiation
```java
@Entity
@Table(name = "contract_negotiation")
public class ContractNegotiation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_contract_id")
    private Contract currentContract;
    
    @Enumerated(EnumType.STRING)
    private NegotiationType type; // NEW_CONTRACT, RENEWAL, RENEGOTIATION
    
    @Enumerated(EnumType.STRING)
    private NegotiationStatus status; // PENDING, IN_PROGRESS, ACCEPTED, REJECTED, EXPIRED
    
    // Club's offer
    private BigDecimal offeredWeeklySalary;
    private BigDecimal offeredSigningBonus;
    private BigDecimal offeredLoyaltyBonus;
    private Integer offeredContractYears;
    private BigDecimal offeredReleaseClause;
    
    // Player's demands
    private BigDecimal demandedWeeklySalary;
    private BigDecimal demandedSigningBonus;
    private BigDecimal demandedLoyaltyBonus;
    private Integer demandedContractYears;
    private BigDecimal demandedReleaseClause;
    
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private LocalDateTime lastOfferDate;
    
    private Integer roundsOfNegotiation;
    private String rejectionReason;
    
    @OneToMany(mappedBy = "negotiation", cascade = CascadeType.ALL)
    private List<NegotiationOffer> offers = new ArrayList<>();
}
```

#### New Entity: NegotiationOffer
```java
@Entity
@Table(name = "negotiation_offer")
public class NegotiationOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id")
    private ContractNegotiation negotiation;
    
    @Enumerated(EnumType.STRING)
    private OfferSide offerSide; // CLUB, PLAYER
    
    private BigDecimal weeklySalary;
    private BigDecimal signingBonus;
    private BigDecimal loyaltyBonus;
    private Integer contractYears;
    private BigDecimal releaseClause;
    
    private LocalDateTime offerDate;
    
    @Enumerated(EnumType.STRING)
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, COUNTERED
    
    private String notes;
}
```

#### Enums to Create
```java
public enum ContractStatus {
    ACTIVE, EXPIRED, TERMINATED, NEGOTIATING
}

public enum ClauseType {
    RELEASE_CLAUSE("Release Clause"),
    LOYALTY_BONUS("Loyalty Bonus"),
    APPEARANCE_BONUS("Appearance Bonus"),
    GOAL_BONUS("Goal Bonus"),
    CLEAN_SHEET_BONUS("Clean Sheet Bonus"),
    PROMOTION_CLAUSE("Promotion Clause"),
    RELEGATION_CLAUSE("Relegation Clause"),
    CHAMPIONS_LEAGUE_CLAUSE("Champions League Qualification Clause"),
    IMAGE_RIGHTS("Image Rights"),
    TERMINATION_CLAUSE("Termination Clause");
    
    private final String displayName;
}

public enum BonusType {
    GOALS("Goals Scored"),
    ASSISTS("Assists Made"),
    APPEARANCES("Appearances"),
    CLEAN_SHEETS("Clean Sheets"),
    TEAM_POSITION("League Position"),
    CUP_PROGRESS("Cup Progress"),
    INDIVIDUAL_AWARD("Individual Award");
    
    private final String displayName;
}

public enum NegotiationType {
    NEW_CONTRACT, RENEWAL, RENEGOTIATION
}

public enum NegotiationStatus {
    PENDING, IN_PROGRESS, ACCEPTED, REJECTED, EXPIRED
}

public enum OfferSide {
    CLUB, PLAYER
}

public enum OfferStatus {
    PENDING, ACCEPTED, REJECTED, COUNTERED
}
```

#### Player Entity Updates
```java
// Add to Player.java
@OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
private Contract currentContract;

@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
private List<Contract> contractHistory = new ArrayList<>();

@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
private List<ContractNegotiation> negotiations = new ArrayList<>();

@Transient
public boolean isContractExpiringSoon() {
    if (currentContract == null) return true;
    return currentContract.getEndDate().isBefore(LocalDate.now().plusMonths(6));
}

@Transient
public BigDecimal getWeeklySalary() {
    return currentContract != null ? currentContract.getWeeklySalary() : BigDecimal.ZERO;
}

@Transient
public boolean hasReleaseClause() {
    return currentContract != null && currentContract.getHasReleaseClause();
}
```

### Service Layer Implementation

#### ContractService
```java
@Service
public class ContractService {
    
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    private ContractNegotiationRepository negotiationRepository;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private ClubService clubService;
    
    /**
     * Start contract negotiation
     */
    public ContractNegotiation startNegotiation(Long playerId, Long clubId, 
                                              NegotiationType type, ContractOfferRequest initialOffer) {
        Player player = playerService.findById(playerId);
        Club club = clubService.findById(clubId);
        
        // Check if there's already an active negotiation
        Optional<ContractNegotiation> existingNegotiation = negotiationRepository
            .findByPlayerAndClubAndStatus(player, club, NegotiationStatus.IN_PROGRESS);
            
        if (existingNegotiation.isPresent()) {
            throw new IllegalStateException("Negotiation already in progress");
        }
        
        // Calculate player demands based on current market value, performance, etc.
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
            .expiryDate(LocalDateTime.now().plusDays(7)) // 7 days to negotiate
            .roundsOfNegotiation(0)
            .build();
            
        negotiation = negotiationRepository.save(negotiation);
        
        // Create initial club offer
        createNegotiationOffer(negotiation, OfferSide.CLUB, initialOffer);
        
        return negotiation;
    }
    
    /**
     * Make counter offer during negotiation
     */
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
        
        // Update negotiation with new offer
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
        
        // Create offer record
        NegotiationOffer negotiationOffer = createNegotiationOffer(negotiation, offerSide, offer);
        
        // Check if offer should be automatically accepted/rejected
        processOfferResponse(negotiation, negotiationOffer);
        
        return negotiationOffer;
    }
    
    /**
     * Accept contract offer and create contract
     */
    public Contract acceptOffer(Long negotiationId) {
        ContractNegotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new EntityNotFoundException("Negotiation not found"));
            
        if (negotiation.getStatus() != NegotiationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Negotiation is not active");
        }
        
        // Create new contract based on final agreed terms
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
        
        // Terminate old contract if exists
        if (negotiation.getCurrentContract() != null) {
            Contract oldContract = negotiation.getCurrentContract();
            oldContract.setStatus(ContractStatus.TERMINATED);
            contractRepository.save(oldContract);
        }
        
        // Update player
        Player player = negotiation.getPlayer();
        player.setCurrentContract(contract);
        player.setSalary(contract.getWeeklySalary().multiply(BigDecimal.valueOf(52))); // Annual salary
        playerService.save(player);
        
        // Update negotiation status
        negotiation.setStatus(NegotiationStatus.ACCEPTED);
        negotiationRepository.save(negotiation);
        
        // Pay signing bonus
        if (contract.getSigningBonus().compareTo(BigDecimal.ZERO) > 0) {
            processSigningBonus(contract);
        }
        
        // Create contract clauses and bonuses
        createDefaultContractClauses(contract);
        
        return contract;
    }
    
    /**
     * Reject contract offer
     */
    public void rejectOffer(Long negotiationId, String reason) {
        ContractNegotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new EntityNotFoundException("Negotiation not found"));
            
        negotiation.setStatus(NegotiationStatus.REJECTED);
        negotiation.setRejectionReason(reason);
        negotiationRepository.save(negotiation);
    }
    
    /**
     * Calculate player contract demands
     */
    private ContractDemands calculatePlayerDemands(Player player, NegotiationType type) {
        // Base demands on player's current market value, age, performance, etc.
        BigDecimal baseWeeklySalary = calculateBaseWeeklySalary(player);
        
        // Adjust based on negotiation type
        double multiplier = switch (type) {
            case NEW_CONTRACT -> 1.0;
            case RENEWAL -> 1.1; // 10% increase for renewals
            case RENEGOTIATION -> 1.2; // 20% increase for renegotiations
        };
        
        BigDecimal demandedSalary = baseWeeklySalary.multiply(BigDecimal.valueOf(multiplier));
        
        // Calculate other demands
        BigDecimal signingBonus = demandedSalary.multiply(BigDecimal.valueOf(10)); // 10 weeks salary
        BigDecimal loyaltyBonus = demandedSalary.multiply(BigDecimal.valueOf(5)); // 5 weeks salary
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
    
    /**
     * Process automatic offer response based on AI logic
     */
    private void processOfferResponse(ContractNegotiation negotiation, NegotiationOffer offer) {
        if (offer.getOfferSide() == OfferSide.CLUB) {
            // Player AI decides whether to accept, reject, or counter
            OfferResponse response = calculatePlayerResponse(negotiation, offer);
            
            switch (response.getDecision()) {
                case ACCEPT -> {
                    offer.setStatus(OfferStatus.ACCEPTED);
                    negotiation.setStatus(NegotiationStatus.ACCEPTED);
                }
                case REJECT -> {
                    offer.setStatus(OfferStatus.REJECTED);
                    negotiation.setStatus(NegotiationStatus.REJECTED);
                    negotiation.setRejectionReason(response.getReason());
                }
                case COUNTER -> {
                    offer.setStatus(OfferStatus.COUNTERED);
                    // AI will make counter offer
                    scheduleAICounterOffer(negotiation);
                }
            }
        } else {
            // Club AI decides (for AI-controlled clubs)
            if (!negotiation.getClub().getUser().isHuman()) {
                OfferResponse response = calculateClubResponse(negotiation, offer);
                // Process club AI response...
            }
        }
        
        negotiationRepository.save(negotiation);
    }
    
    /**
     * Create performance bonuses for contract
     */
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
            
        // Create trigger condition based on bonus type
        String triggerCondition = createBonusTriggerCondition(type, targetValue);
        bonus.setTriggerCondition(triggerCondition);
        
        contract.getPerformanceBonuses().add(bonus);
        contractRepository.save(contract);
    }
    
    /**
     * Check and process performance bonuses
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void processPerformanceBonuses() {
        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);
        
        for (Contract contract : activeContracts) {
            for (PerformanceBonus bonus : contract.getPerformanceBonuses()) {
                if (!bonus.getIsAchieved() && checkBonusCondition(bonus)) {
                    // Award bonus
                    bonus.setIsAchieved(true);
                    bonus.setAchievedDate(LocalDate.now());
                    
                    // Pay bonus to player/club
                    processPerformanceBonus(bonus);
                    
                    // Create news/notification
                    createBonusAchievementNews(bonus);
                }
            }
        }
    }
    
    /**
     * Handle contract expiry
     */
    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    public void processContractExpiries() {
        List<Contract> expiringContracts = contractRepository
            .findByStatusAndEndDateBefore(ContractStatus.ACTIVE, LocalDate.now());
            
        for (Contract contract : expiringContracts) {
            // Check for automatic extension
            if (shouldAutoExtend(contract)) {
                extendContract(contract);
            } else {
                // Expire contract
                contract.setStatus(ContractStatus.EXPIRED);
                contractRepository.save(contract);
                
                // Make player a free agent
                Player player = contract.getPlayer();
                player.setCurrentContract(null);
                player.setTeam(null);
                playerService.save(player);
                
                // Create news about contract expiry
                createContractExpiryNews(contract);
            }
        }
    }
    
    /**
     * Trigger release clause
     */
    public TransferOffer triggerReleaseClause(Long playerId, Long buyingClubId) {
        Player player = playerService.findById(playerId);
        Club buyingClub = clubService.findById(buyingClubId);
        
        Contract contract = player.getCurrentContract();
        if (contract == null || !contract.getHasReleaseClause()) {
            throw new IllegalStateException("Player has no release clause");
        }
        
        BigDecimal releaseClauseAmount = contract.getReleaseClause();
        
        // Check if buying club can afford the release clause
        if (buyingClub.getFinance().getBalance().compareTo(releaseClauseAmount) < 0) {
            throw new InsufficientFundsException("Club cannot afford release clause");
        }
        
        // Create automatic transfer offer at release clause amount
        TransferOffer offer = TransferOffer.builder()
            .player(player)
            .buyingClub(buyingClub)
            .sellingClub(player.getTeam().getClub())
            .offerAmount(releaseClauseAmount)
            .isReleaseClause(true)
            .status(TransferOfferStatus.ACCEPTED) // Automatically accepted
            .offerDate(LocalDateTime.now())
            .build();
            
        return transferService.processTransferOffer(offer);
    }
    
    private BigDecimal calculateBaseWeeklySalary(Player player) {
        // Calculate based on player average, age, position, market value
        double playerAverage = player.getAverage();
        int age = player.getAge();
        PlayerRole position = player.getRole();
        
        // Base salary calculation
        double baseSalary = playerAverage * 1000; // $1000 per average point per week
        
        // Age adjustments
        if (age < 23) baseSalary *= 0.8; // Young players earn less
        else if (age > 30) baseSalary *= 0.9; // Older players earn slightly less
        
        // Position adjustments
        switch (position) {
            case GOALKEEPER -> baseSalary *= 0.9;
            case DEFENDER -> baseSalary *= 0.95;
            case MIDFIELDER -> baseSalary *= 1.0;
            case WING -> baseSalary *= 1.05;
            case FORWARD -> baseSalary *= 1.1;
        }
        
        return BigDecimal.valueOf(baseSalary).setScale(0, RoundingMode.HALF_UP);
    }
    
    private Integer calculateDesiredContractLength(Player player) {
        int age = player.getAge();
        
        if (age < 25) return 4; // Young players want longer contracts
        else if (age < 30) return 3; // Prime players want medium contracts
        else if (age < 35) return 2; // Older players want shorter contracts
        else return 1; // Very old players want 1 year contracts
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
}
```

### API Endpoints

#### ContractController
```java
@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    
    @Autowired
    private ContractService contractService;
    
    @GetMapping("/player/{playerId}")
    public ResponseEntity<ContractDTO> getPlayerContract(@PathVariable Long playerId) {
        Contract contract = contractService.getPlayerCurrentContract(playerId);
        return ResponseEntity.ok(convertToDTO(contract));
    }
    
    @PostMapping("/negotiate")
    public ResponseEntity<ContractNegotiationDTO> startNegotiation(
            @RequestBody StartNegotiationRequest request) {
        ContractNegotiation negotiation = contractService.startNegotiation(
            request.getPlayerId(),
            request.getClubId(),
            request.getType(),
            request.getInitialOffer()
        );
        return ResponseEntity.ok(convertToDTO(negotiation));
    }
    
    @PostMapping("/negotiate/{negotiationId}/offer")
    public ResponseEntity<NegotiationOfferDTO> makeCounterOffer(
            @PathVariable Long negotiationId,
            @RequestBody CounterOfferRequest request) {
        NegotiationOffer offer = contractService.makeCounterOffer(
            negotiationId,
            request.getOfferSide(),
            request.getOffer()
        );
        return ResponseEntity.ok(convertToDTO(offer));
    }
    
    @PostMapping("/negotiate/{negotiationId}/accept")
    public ResponseEntity<ContractDTO> acceptOffer(@PathVariable Long negotiationId) {
        Contract contract = contractService.acceptOffer(negotiationId);
        return ResponseEntity.ok(convertToDTO(contract));
    }
    
    @PostMapping("/negotiate/{negotiationId}/reject")
    public ResponseEntity<Void> rejectOffer(
            @PathVariable Long negotiationId,
            @RequestBody RejectOfferRequest request) {
        contractService.rejectOffer(negotiationId, request.getReason());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/club/{clubId}/negotiations")
    public ResponseEntity<List<ContractNegotiationDTO>> getClubNegotiations(
            @PathVariable Long clubId,
            @RequestParam(required = false) NegotiationStatus status) {
        List<ContractNegotiation> negotiations = contractService.getClubNegotiations(clubId, status);
        return ResponseEntity.ok(negotiations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/{contractId}/bonus")
    public ResponseEntity<Void> addPerformanceBonus(
            @PathVariable Long contractId,
            @RequestBody AddPerformanceBonusRequest request) {
        contractService.addPerformanceBonus(
            contractId,
            request.getType(),
            request.getTargetValue(),
            request.getBonusAmount(),
            request.getDescription()
        );
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/release-clause/{playerId}")
    public ResponseEntity<TransferOfferDTO> triggerReleaseClause(
            @PathVariable Long playerId,
            @RequestBody TriggerReleaseClauseRequest request) {
        TransferOffer offer = contractService.triggerReleaseClause(playerId, request.getBuyingClubId());
        return ResponseEntity.ok(convertToDTO(offer));
    }
    
    @GetMapping("/expiring")
    public ResponseEntity<List<ContractDTO>> getExpiringContracts(
            @RequestParam(defaultValue = "6") int monthsAhead) {
        List<Contract> contracts = contractService.getExpiringContracts(monthsAhead);
        return ResponseEntity.ok(contracts.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### ContractNegotiation Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { startNegotiation, makeCounterOffer, acceptOffer, rejectOffer } from '../services/api';

const ContractNegotiation = ({ playerId, clubId, onClose }) => {
    const [negotiation, setNegotiation] = useState(null);
    const [currentOffer, setCurrentOffer] = useState({
        weeklySalary: 0,
        signingBonus: 0,
        loyaltyBonus: 0,
        contractYears: 3,
        releaseClause: 0
    });
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        initializeNegotiation();
    }, [playerId, clubId]);

    const initializeNegotiation = async () => {
        setLoading(true);
        try {
            const response = await startNegotiation({
                playerId,
                clubId,
                type: 'NEW_CONTRACT',
                initialOffer: currentOffer
            });
            setNegotiation(response.data);
        } catch (error) {
            console.error('Error starting negotiation:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleMakeOffer = async () => {
        setLoading(true);
        try {
            const response = await makeCounterOffer(negotiation.id, {
                offerSide: 'CLUB',
                offer: currentOffer
            });
            
            // Refresh negotiation data
            setNegotiation(prev => ({
                ...prev,
                offeredWeeklySalary: currentOffer.weeklySalary,
                offeredSigningBonus: currentOffer.signingBonus,
                offeredLoyaltyBonus: currentOffer.loyaltyBonus,
                offeredContractYears: currentOffer.contractYears,
                offeredReleaseClause: currentOffer.releaseClause,
                roundsOfNegotiation: prev.roundsOfNegotiation + 1
            }));
        } catch (error) {
            console.error('Error making offer:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAcceptOffer = async () => {
        setLoading(true);
        try {
            await acceptOffer(negotiation.id);
            // Show success message and close
            onClose();
        } catch (error) {
            console.error('Error accepting offer:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRejectOffer = async (reason) => {
        setLoading(true);
        try {
            await rejectOffer(negotiation.id, { reason });
            onClose();
        } catch (error) {
            console.error('Error rejecting offer:', error);
        } finally {
            setLoading(false);
        }
    };

    const calculateOfferDifference = (offered, demanded) => {
        const difference = ((offered - demanded) / demanded) * 100;
        return difference.toFixed(1);
    };

    const getOfferStatus = (offered, demanded) => {
        const difference = (offered - demanded) / demanded;
        if (difference >= 0.1) return 'generous';
        if (difference >= 0) return 'fair';
        if (difference >= -0.1) return 'close';
        return 'low';
    };

    if (loading) return <div>Loading negotiation...</div>;
    if (!negotiation) return <div>Failed to start negotiation</div>;

    return (
        <div className="contract-negotiation">
            <div className="negotiation-header">
                <h2>Contract Negotiation</h2>
                <div className="player-info">
                    <h3>{negotiation.player.name} {negotiation.player.surname}</h3>
                    <span>Round {negotiation.roundsOfNegotiation + 1}</span>
                </div>
            </div>

            <div className="negotiation-content">
                <div className="offers-comparison">
                    <div className="offer-section">
                        <h4>Your Offer</h4>
                        <div className="offer-details">
                            <div className="offer-item">
                                <label>Weekly Salary:</label>
                                <input
                                    type="number"
                                    value={currentOffer.weeklySalary}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        weeklySalary: parseInt(e.target.value)
                                    }))}
                                />
                                <span className={`difference ${getOfferStatus(currentOffer.weeklySalary, negotiation.demandedWeeklySalary)}`}>
                                    {calculateOfferDifference(currentOffer.weeklySalary, negotiation.demandedWeeklySalary)}%
                                </span>
                            </div>

                            <div className="offer-item">
                                <label>Signing Bonus:</label>
                                <input
                                    type="number"
                                    value={currentOffer.signingBonus}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        signingBonus: parseInt(e.target.value)
                                    }))}
                                />
                                <span className={`difference ${getOfferStatus(currentOffer.signingBonus, negotiation.demandedSigningBonus)}`}>
                                    {calculateOfferDifference(currentOffer.signingBonus, negotiation.demandedSigningBonus)}%
                                </span>
                            </div>

                            <div className="offer-item">
                                <label>Contract Years:</label>
                                <select
                                    value={currentOffer.contractYears}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        contractYears: parseInt(e.target.value)
                                    }))}
                                >
                                    <option value={1}>1 Year</option>
                                    <option value={2}>2 Years</option>
                                    <option value={3}>3 Years</option>
                                    <option value={4}>4 Years</option>
                                    <option value={5}>5 Years</option>
                                </select>
                                <span className={`difference ${getOfferStatus(currentOffer.contractYears, negotiation.demandedContractYears)}`}>
                                    {currentOffer.contractYears >= negotiation.demandedContractYears ? '✓' : '✗'}
                                </span>
                            </div>

                            <div className="offer-item">
                                <label>Release Clause:</label>
                                <input
                                    type="number"
                                    value={currentOffer.releaseClause}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        releaseClause: parseInt(e.target.value)
                                    }))}
                                />
                                <span className={`difference ${getOfferStatus(currentOffer.releaseClause, negotiation.demandedReleaseClause)}`}>
                                    {currentOffer.releaseClause >= negotiation.demandedReleaseClause ? '✓' : '✗'}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="demand-section">
                        <h4>Player Demands</h4>
                        <div className="demand-details">
                            <div className="demand-item">
                                <label>Weekly Salary:</label>
                                <span>${negotiation.demandedWeeklySalary.toLocaleString()}</span>
                            </div>
                            <div className="demand-item">
                                <label>Signing Bonus:</label>
                                <span>${negotiation.demandedSigningBonus.toLocaleString()}</span>
                            </div>
                            <div className="demand-item">
                                <label>Contract Years:</label>
                                <span>{negotiation.demandedContractYears} years</span>
                            </div>
                            <div className="demand-item">
                                <label>Release Clause:</label>
                                <span>${negotiation.demandedReleaseClause.toLocaleString()}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="negotiation-progress">
                    <div className="progress-bar">
                        <div 
                            className="progress-fill"
                            style={{ width: `${Math.min(100, (negotiation.roundsOfNegotiation / 10) * 100)}%` }}
                        ></div>
                    </div>
                    <span>Negotiation Progress: {negotiation.roundsOfNegotiation}/10 rounds</span>
                </div>

                <div className="total-cost">
                    <h4>Total Contract Cost</h4>
                    <div className="cost-breakdown">
                        <div>Weekly Salary: ${currentOffer.weeklySalary.toLocaleString()}</div>
                        <div>Annual Salary: ${(currentOffer.weeklySalary * 52).toLocaleString()}</div>
                        <div>Total Contract: ${(currentOffer.weeklySalary * 52 * currentOffer.contractYears + currentOffer.signingBonus).toLocaleString()}</div>
                    </div>
                </div>
            </div>

            <div className="negotiation-actions">
                <button 
                    className="make-offer-btn"
                    onClick={handleMakeOffer}
                    disabled={loading}
                >
                    Make Offer
                </button>
                
                <button 
                    className="accept-btn"
                    onClick={handleAcceptOffer}
                    disabled={loading}
                >
                    Accept Current Terms
                </button>
                
                <button 
                    className="reject-btn"
                    onClick={() => handleRejectOffer('Terms not acceptable')}
                    disabled={loading}
                >
                    End Negotiation
                </button>
            </div>

            <div className="negotiation-tips">
                <h4>Negotiation Tips</h4>
                <ul>
                    <li>Players value security - longer contracts may reduce salary demands</li>
                    <li>Signing bonuses can help close deals without increasing weekly wages</li>
                    <li>Release clauses protect the club but may increase other demands</li>
                    <li>Player age and performance affect their negotiating power</li>
                </ul>
            </div>
        </div>
    );
};

export default ContractNegotiation;
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class ContractServiceTest {
    
    @Mock
    private ContractRepository contractRepository;
    
    @Mock
    private ContractNegotiationRepository negotiationRepository;
    
    @InjectMocks
    private ContractService contractService;
    
    @Test
    void testNegotiationStart() {
        Player player = createTestPlayer();
        Club club = createTestClub();
        ContractOfferRequest offer = createTestOffer();
        
        ContractNegotiation negotiation = contractService.startNegotiation(
            player.getId(), club.getId(), NegotiationType.NEW_CONTRACT, offer);
        
        assertThat(negotiation.getPlayer()).isEqualTo(player);
        assertThat(negotiation.getClub()).isEqualTo(club);
        assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.IN_PROGRESS);
    }
    
    @Test
    void testContractCreation() {
        ContractNegotiation negotiation = createTestNegotiation();
        
        Contract contract = contractService.acceptOffer(negotiation.getId());
        
        assertThat(contract.getPlayer()).isEqualTo(negotiation.getPlayer());
        assertThat(contract.getWeeklySalary()).isEqualTo(negotiation.getOfferedWeeklySalary());
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
    }
    
    @Test
    void testPerformanceBonusCheck() {
        Contract contract = createTestContract();
        PerformanceBonus bonus = createTestBonus(BonusType.GOALS, 10);
        contract.getPerformanceBonuses().add(bonus);
        
        // Mock player stats
        PlayerSeasonStats stats = createTestStats(15, 5); // 15 goals, 5 assists
        when(playerHistoryService.getCurrentSeasonStats(any())).thenReturn(stats);
        
        boolean achieved = contractService.checkBonusCondition(bonus);
        
        assertThat(achieved).isTrue();
    }
}
```

### Configuration

#### Application Properties
```properties
# Contract system configuration
fm.contracts.negotiation.max-rounds=10
fm.contracts.negotiation.expiry-days=7
fm.contracts.bonus.check.time=02:00
fm.contracts.expiry.check.time=01:00
fm.contracts.auto-extension.enabled=true
fm.contracts.release-clause.min-multiplier=2.0
```

## Implementation Notes

1. **AI Negotiation**: Implement sophisticated AI for realistic contract negotiations
2. **Market Dynamics**: Contract demands should reflect current market conditions
3. **Performance Tracking**: Link performance bonuses to actual player statistics
4. **Financial Impact**: Ensure contract costs affect club finances realistically
5. **Legal Constraints**: Consider real-world contract regulations and limits
6. **Player Happiness**: Contract terms should affect player morale and loyalty
7. **Agent System**: Future enhancement for player agents in negotiations

## Dependencies

- Player service for player data and updates
- Club financial system for salary payments and bonuses
- Player history system for performance bonus tracking
- Transfer system for release clause integration
- News system for contract-related announcements
- Notification system for contract expiry warnings
- AI system for automated negotiation responses