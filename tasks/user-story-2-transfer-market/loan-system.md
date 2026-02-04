# Loan System Implementation

## Overview
Implement a comprehensive loan system for temporary player transfers between clubs with salary sharing options and development tracking.

## Technical Requirements

### Database Schema Changes

#### New Entity: LoanAgreement
```java
@Entity
@Table(name = "loan_agreement")
public class LoanAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_club_id")
    private Club parentClub; // Club that owns the player
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_club_id")
    private Club loanClub; // Club receiving the player on loan
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private LoanStatus status; // PROPOSED, ACTIVE, COMPLETED, TERMINATED, RECALLED
    
    // Financial terms
    private BigDecimal loanFee; // One-time fee paid by loan club
    private Double parentClubSalaryShare; // 0.0 to 1.0 (percentage parent club pays)
    private Double loanClubSalaryShare; // 0.0 to 1.0 (percentage loan club pays)
    
    // Loan conditions
    private Boolean hasRecallClause; // Can parent club recall player?
    private LocalDate earliestRecallDate;
    private Boolean hasOptionToBuy; // Option to buy at end of loan
    private BigDecimal optionToBuyPrice;
    private Boolean hasObligationToBuy; // Mandatory purchase if conditions met
    private String obligationConditions; // JSON conditions for mandatory purchase
    
    // Performance tracking
    private Integer minimumAppearances; // Minimum games player must play
    private Integer actualAppearances;
    private Boolean developmentTargetsMet;
    private String developmentTargets; // JSON development goals
    
    // Agreement details
    private String loanReason; // Why player is being loaned
    private String specialConditions; // Additional terms
    private LocalDateTime agreementDate;
    
    @OneToMany(mappedBy = "loanAgreement", cascade = CascadeType.ALL)
    private List<LoanPerformanceReview> performanceReviews = new ArrayList<>();
}
```

#### New Entity: LoanProposal
```java
@Entity
@Table(name = "loan_proposal")
public class LoanProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposing_club_id")
    private Club proposingClub; // Club making the loan request
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_club_id")
    private Club targetClub; // Club that owns the player
    
    @Enumerated(EnumType.STRING)
    private ProposalStatus status; // PENDING, ACCEPTED, REJECTED, WITHDRAWN, EXPIRED
    
    // Proposed terms
    private LocalDate proposedStartDate;
    private LocalDate proposedEndDate;
    private BigDecimal proposedLoanFee;
    private Double proposedSalaryShare;
    private Boolean proposedRecallClause;
    private Boolean proposedOptionToBuy;
    private BigDecimal proposedOptionPrice;
    
    private String proposalMessage;
    private String rejectionReason;
    
    private LocalDateTime proposalDate;
    private LocalDateTime responseDate;
    private LocalDateTime expiryDate;
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL)
    private List<LoanNegotiation> negotiations = new ArrayList<>();
}
```

#### New Entity: LoanPerformanceReview
```java
@Entity
@Table(name = "loan_performance_review")
public class LoanPerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_agreement_id")
    private LoanAgreement loanAgreement;
    
    private LocalDate reviewDate;
    
    @Enumerated(EnumType.STRING)
    private ReviewPeriod period; // MONTHLY, QUARTERLY, MID_SEASON, END_SEASON
    
    // Performance metrics
    private Integer matchesPlayed;
    private Integer goals;
    private Integer assists;
    private Double averageRating;
    private Integer yellowCards;
    private Integer redCards;
    
    // Development assessment
    private Double skillImprovement; // Overall skill improvement
    private String developmentNotes;
    private Boolean targetsMet;
    
    // Recommendations
    @Enumerated(EnumType.STRING)
    private LoanRecommendation recommendation; // CONTINUE, RECALL, EXTEND, PURCHASE
    
    private String reviewNotes;
    private String parentClubFeedback;
    private String loanClubFeedback;
}
```

#### Enums to Create
```java
public enum LoanStatus {
    PROPOSED("Proposed"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    TERMINATED("Terminated Early"),
    RECALLED("Recalled by Parent Club");
    
    private final String displayName;
}

public enum ProposalStatus {
    PENDING, ACCEPTED, REJECTED, WITHDRAWN, EXPIRED
}

public enum ReviewPeriod {
    MONTHLY("Monthly Review"),
    QUARTERLY("Quarterly Review"),
    MID_SEASON("Mid-Season Review"),
    END_SEASON("End of Season Review");
    
    private final String displayName;
}

public enum LoanRecommendation {
    CONTINUE("Continue Loan"),
    RECALL("Recall Player"),
    EXTEND("Extend Loan"),
    PURCHASE("Activate Purchase Option");
    
    private final String displayName;
}
```

### Service Layer Implementation

#### LoanService
```java
@Service
public class LoanService {
    
    @Autowired
    private LoanAgreementRepository loanAgreementRepository;
    
    @Autowired
    private LoanProposalRepository loanProposalRepository;
    
    @Autowired
    private LoanPerformanceReviewRepository performanceReviewRepository;
    
    /**
     * Create loan proposal
     */
    public LoanProposal createLoanProposal(CreateLoanProposalRequest request) {
        Player player = playerService.findById(request.getPlayerId());
        Club proposingClub = clubService.findById(request.getProposingClubId());
        Club targetClub = player.getTeam().getClub();
        
        // Validate loan eligibility
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
            .expiryDate(LocalDateTime.now().plusDays(7)) // 7 days to respond
            .build();
            
        proposal = loanProposalRepository.save(proposal);
        
        // Notify target club
        createLoanProposalNotification(proposal);
        
        return proposal;
    }
    
    /**
     * Accept loan proposal and create agreement
     */
    public LoanAgreement acceptLoanProposal(Long proposalId) {
        LoanProposal proposal = loanProposalRepository.findById(proposalId)
            .orElseThrow(() -> new EntityNotFoundException("Loan proposal not found"));
            
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal is not pending");
        }
        
        // Create loan agreement
        LoanAgreement agreement = LoanAgreement.builder()
            .player(proposal.getPlayer())
            .parentClub(proposal.getTargetClub())
            .loanClub(proposal.getProposingClub())
            .startDate(proposal.getProposedStartDate())
            .endDate(proposal.getProposedEndDate())
            .status(LoanStatus.ACTIVE)
            .loanFee(proposal.getProposedLoanFee())
            .loanClubSalaryShare(proposal.getProposedSalaryShare())
            .parentClubSalaryShare(1.0 - proposal.getProposedSalaryShare())
            .hasRecallClause(proposal.getProposedRecallClause())
            .hasOptionToBuy(proposal.getProposedOptionToBuy())
            .optionToBuyPrice(proposal.getProposedOptionPrice())
            .agreementDate(LocalDateTime.now())
            .actualAppearances(0)
            .build();
            
        agreement = loanAgreementRepository.save(agreement);
        
        // Update proposal status
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setResponseDate(LocalDateTime.now());
        loanProposalRepository.save(proposal);
        
        // Transfer player to loan club
        transferPlayerOnLoan(agreement);
        
        // Process loan fee payment
        processLoanFeePayment(agreement);
        
        return agreement;
    }
    
    /**
     * Process monthly loan reviews
     */
    @Scheduled(cron = "0 0 9 1 * *") // First day of month at 9 AM
    public void processMonthlyLoanReviews() {
        List<LoanAgreement> activeLoans = loanAgreementRepository
            .findByStatus(LoanStatus.ACTIVE);
            
        for (LoanAgreement loan : activeLoans) {
            if (shouldCreateMonthlyReview(loan)) {
                createPerformanceReview(loan, ReviewPeriod.MONTHLY);
            }
        }
    }
    
    /**
     * Create performance review
     */
    public LoanPerformanceReview createPerformanceReview(LoanAgreement loan, 
                                                        ReviewPeriod period) {
        Player player = loan.getPlayer();
        
        // Get performance statistics for review period
        PlayerSeasonStats stats = playerHistoryService.getCurrentSeasonStats(player.getId());
        
        LoanPerformanceReview review = LoanPerformanceReview.builder()
            .loanAgreement(loan)
            .reviewDate(LocalDate.now())
            .period(period)
            .matchesPlayed(stats.getMatchesPlayed())
            .goals(stats.getGoals())
            .assists(stats.getAssists())
            .averageRating(stats.getAverageRating())
            .yellowCards(stats.getYellowCards())
            .redCards(stats.getRedCards())
            .skillImprovement(calculateSkillImprovement(player, loan.getStartDate()))
            .targetsMet(evaluateDevelopmentTargets(loan))
            .recommendation(generateLoanRecommendation(loan, stats))
            .build();
            
        review = performanceReviewRepository.save(review);
        
        // Update loan agreement
        loan.setActualAppearances(stats.getMatchesPlayed());
        loanAgreementRepository.save(loan);
        
        // Notify both clubs about review
        createPerformanceReviewNotification(review);
        
        return review;
    }
    
    /**
     * Recall player from loan
     */
    public void recallPlayerFromLoan(Long loanId, String reason) {
        LoanAgreement loan = loanAgreementRepository.findById(loanId)
            .orElseThrow(() -> new EntityNotFoundException("Loan agreement not found"));
            
        if (!loan.getHasRecallClause()) {
            throw new IllegalStateException("Loan agreement has no recall clause");
        }
        
        if (LocalDate.now().isBefore(loan.getEarliestRecallDate())) {
            throw new IllegalStateException("Cannot recall player before earliest recall date");
        }
        
        // Update loan status
        loan.setStatus(LoanStatus.RECALLED);
        loanAgreementRepository.save(loan);
        
        // Transfer player back to parent club
        transferPlayerBackFromLoan(loan);
        
        // Create final performance review
        createPerformanceReview(loan, ReviewPeriod.END_SEASON);
        
        // Notify clubs about recall
        createLoanRecallNotification(loan, reason);
    }
    
    /**
     * Activate purchase option
     */
    public TransferOffer activatePurchaseOption(Long loanId) {
        LoanAgreement loan = loanAgreementRepository.findById(loanId)
            .orElseThrow(() -> new EntityNotFoundException("Loan agreement not found"));
            
        if (!loan.getHasOptionToBuy()) {
            throw new IllegalStateException("Loan has no purchase option");
        }
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Loan is not active");
        }
        
        // Create transfer offer at agreed price
        TransferOffer offer = TransferOffer.builder()
            .player(loan.getPlayer())
            .buyingClub(loan.getLoanClub())
            .sellingClub(loan.getParentClub())
            .offerAmount(loan.getOptionToBuyPrice())
            .isLoanPurchaseOption(true)
            .status(TransferOfferStatus.ACCEPTED) // Automatically accepted
            .offerDate(LocalDateTime.now())
            .build();
            
        // Process transfer
        TransferOffer processedOffer = transferService.processTransferOffer(offer);
        
        // Complete loan agreement
        loan.setStatus(LoanStatus.COMPLETED);
        loanAgreementRepository.save(loan);
        
        return processedOffer;
    }
    
    private void validateLoanEligibility(Player player, Club proposingClub) {
        // Check if player is already on loan
        if (isPlayerOnLoan(player)) {
            throw new IllegalStateException("Player is already on loan");
        }
        
        // Check if player belongs to proposing club
        if (player.getTeam().getClub().equals(proposingClub)) {
            throw new IllegalStateException("Cannot loan own player");
        }
        
        // Check if player is injured
        if (player.isInjured()) {
            throw new IllegalStateException("Cannot loan injured player");
        }
        
        // Check loan window (if applicable)
        if (!isLoanWindowOpen()) {
            throw new IllegalStateException("Loan window is closed");
        }
    }
    
    private void transferPlayerOnLoan(LoanAgreement agreement) {
        Player player = agreement.getPlayer();
        
        // Store original team for recall
        player.setOriginalTeam(player.getTeam());
        
        // Transfer to loan club
        player.setTeam(agreement.getLoanClub().getTeam());
        playerService.save(player);
        
        // Record transfer history
        playerHistoryService.recordTransfer(player, 
                                          agreement.getParentClub(), 
                                          agreement.getLoanClub(),
                                          BigDecimal.ZERO, // No transfer fee for loans
                                          TransferType.LOAN);
    }
    
    private LoanRecommendation generateLoanRecommendation(LoanAgreement loan, 
                                                        PlayerSeasonStats stats) {
        // Evaluate performance
        boolean performingWell = stats.getAverageRating() > 7.0;
        boolean gettingPlayingTime = stats.getMatchesPlayed() > 10;
        boolean developmentTargetsMet = evaluateDevelopmentTargets(loan);
        
        // Check if loan is near end
        boolean nearEndOfLoan = ChronoUnit.DAYS.between(LocalDate.now(), 
                                                       loan.getEndDate()) < 60;
        
        if (performingWell && gettingPlayingTime && developmentTargetsMet) {
            if (nearEndOfLoan && loan.getHasOptionToBuy()) {
                return LoanRecommendation.PURCHASE;
            }
            return LoanRecommendation.CONTINUE;
        } else if (!gettingPlayingTime || !performingWell) {
            return LoanRecommendation.RECALL;
        } else if (nearEndOfLoan && performingWell) {
            return LoanRecommendation.EXTEND;
        }
        
        return LoanRecommendation.CONTINUE;
    }
}
```

### API Endpoints

#### LoanController
```java
@RestController
@RequestMapping("/api/loans")
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
    @PostMapping("/proposal")
    public ResponseEntity<LoanProposalDTO> createLoanProposal(
            @RequestBody CreateLoanProposalRequest request) {
        LoanProposal proposal = loanService.createLoanProposal(request);
        return ResponseEntity.ok(convertToDTO(proposal));
    }
    
    @PostMapping("/proposal/{proposalId}/accept")
    public ResponseEntity<LoanAgreementDTO> acceptLoanProposal(@PathVariable Long proposalId) {
        LoanAgreement agreement = loanService.acceptLoanProposal(proposalId);
        return ResponseEntity.ok(convertToDTO(agreement));
    }
    
    @PostMapping("/proposal/{proposalId}/reject")
    public ResponseEntity<Void> rejectLoanProposal(
            @PathVariable Long proposalId,
            @RequestBody RejectProposalRequest request) {
        loanService.rejectLoanProposal(proposalId, request.getReason());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/club/{clubId}/active")
    public ResponseEntity<List<LoanAgreementDTO>> getActiveLoans(@PathVariable Long clubId) {
        List<LoanAgreement> loans = loanService.getClubActiveLoans(clubId);
        return ResponseEntity.ok(loans.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/agreement/{loanId}/recall")
    public ResponseEntity<Void> recallPlayer(
            @PathVariable Long loanId,
            @RequestBody RecallPlayerRequest request) {
        loanService.recallPlayerFromLoan(loanId, request.getReason());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/agreement/{loanId}/purchase")
    public ResponseEntity<TransferOfferDTO> activatePurchaseOption(@PathVariable Long loanId) {
        TransferOffer offer = loanService.activatePurchaseOption(loanId);
        return ResponseEntity.ok(convertToDTO(offer));
    }
    
    @GetMapping("/agreement/{loanId}/reviews")
    public ResponseEntity<List<LoanPerformanceReviewDTO>> getPerformanceReviews(
            @PathVariable Long loanId) {
        List<LoanPerformanceReview> reviews = loanService.getPerformanceReviews(loanId);
        return ResponseEntity.ok(reviews.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### LoanManager Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getActiveLoans, createLoanProposal, recallPlayer } from '../services/api';

const LoanManager = ({ clubId }) => {
    const [activeLoans, setActiveLoans] = useState([]);
    const [selectedTab, setSelectedTab] = useState('outgoing');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadLoanData();
    }, [clubId]);

    const loadLoanData = async () => {
        try {
            const response = await getActiveLoans(clubId);
            setActiveLoans(response.data);
        } catch (error) {
            console.error('Error loading loan data:', error);
        } finally {
            setLoading(false);
        }
    };

    const outgoingLoans = activeLoans.filter(loan => loan.parentClub.id === clubId);
    const incomingLoans = activeLoans.filter(loan => loan.loanClub.id === clubId);

    if (loading) return <div>Loading loan data...</div>;

    return (
        <div className="loan-manager">
            <div className="loan-header">
                <h2>Loan Management</h2>
                <div className="loan-stats">
                    <div className="stat">
                        <span>Players Out:</span>
                        <span>{outgoingLoans.length}</span>
                    </div>
                    <div className="stat">
                        <span>Players In:</span>
                        <span>{incomingLoans.length}</span>
                    </div>
                </div>
            </div>

            <div className="loan-tabs">
                <button 
                    className={selectedTab === 'outgoing' ? 'active' : ''}
                    onClick={() => setSelectedTab('outgoing')}
                >
                    Players Out ({outgoingLoans.length})
                </button>
                <button 
                    className={selectedTab === 'incoming' ? 'active' : ''}
                    onClick={() => setSelectedTab('incoming')}
                >
                    Players In ({incomingLoans.length})
                </button>
            </div>

            {selectedTab === 'outgoing' && (
                <div className="outgoing-loans">
                    {outgoingLoans.map(loan => (
                        <LoanCard key={loan.id} loan={loan} type="outgoing" onUpdate={loadLoanData} />
                    ))}
                </div>
            )}

            {selectedTab === 'incoming' && (
                <div className="incoming-loans">
                    {incomingLoans.map(loan => (
                        <LoanCard key={loan.id} loan={loan} type="incoming" onUpdate={loadLoanData} />
                    ))}
                </div>
            )}
        </div>
    );
};

const LoanCard = ({ loan, type, onUpdate }) => {
    const isOutgoing = type === 'outgoing';
    const otherClub = isOutgoing ? loan.loanClub : loan.parentClub;
    
    return (
        <div className="loan-card">
            <div className="loan-player-info">
                <h4>{loan.player.name} {loan.player.surname}</h4>
                <span className="player-position">{loan.player.role}</span>
                <span className="player-age">Age: {loan.player.age}</span>
            </div>

            <div className="loan-details">
                <div className="detail-row">
                    <span>{isOutgoing ? 'Loaned to:' : 'Loaned from:'}</span>
                    <span>{otherClub.name}</span>
                </div>
                <div className="detail-row">
                    <span>Period:</span>
                    <span>
                        {new Date(loan.startDate).toLocaleDateString()} - 
                        {new Date(loan.endDate).toLocaleDateString()}
                    </span>
                </div>
                <div className="detail-row">
                    <span>Salary Share:</span>
                    <span>
                        {isOutgoing ? 
                            `${Math.round(loan.parentClubSalaryShare * 100)}% (You)` :
                            `${Math.round(loan.loanClubSalaryShare * 100)}% (You)`
                        }
                    </span>
                </div>
            </div>

            <div className="loan-performance">
                <div className="performance-stat">
                    <span>Appearances:</span>
                    <span>{loan.actualAppearances}</span>
                </div>
                {loan.minimumAppearances && (
                    <div className="performance-stat">
                        <span>Required:</span>
                        <span>{loan.minimumAppearances}</span>
                    </div>
                )}
            </div>

            <div className="loan-options">
                {loan.hasRecallClause && isOutgoing && (
                    <span className="option-badge recall">Recall Available</span>
                )}
                {loan.hasOptionToBuy && !isOutgoing && (
                    <span className="option-badge purchase">Purchase Option</span>
                )}
            </div>

            <div className="loan-actions">
                {isOutgoing && loan.hasRecallClause && (
                    <button className="recall-btn">
                        Recall Player
                    </button>
                )}
                {!isOutgoing && loan.hasOptionToBuy && (
                    <button className="purchase-btn">
                        Activate Purchase (${loan.optionToBuyPrice?.toLocaleString()})
                    </button>
                )}
                <button className="review-btn">
                    View Reviews
                </button>
            </div>
        </div>
    );
};

export default LoanManager;
```

## Implementation Notes

1. **Salary Management**: Implement automatic salary sharing between clubs
2. **Performance Tracking**: Regular reviews help evaluate loan success
3. **Recall System**: Balance between flexibility and commitment
4. **Purchase Options**: Provide pathway for permanent transfers
5. **Development Focus**: Loans should benefit young player development
6. **Financial Impact**: Consider loan fees and salary sharing in club finances

## Dependencies

- Transfer system for purchase options
- Contract system for salary management
- Player history system for performance tracking
- Financial system for loan fee processing
- Notification system for loan-related alerts