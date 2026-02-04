# Dynamic Sponsorship System Implementation

## Overview
Implement a dynamic sponsorship system where sponsor offers vary based on club performance, league position, and reputation. Sponsors will provide different types of deals with performance-based bonuses.

## Technical Requirements

### Database Schema Changes

#### Enhanced SponsorshipDeal Entity (Already defined in financial-management.md)
```java
@Entity
@Table(name = "sponsorship_deal")
public class SponsorshipDeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id")
    private Sponsor sponsor;
    
    @Enumerated(EnumType.STRING)
    private SponsorshipType type;
    
    private BigDecimal baseAnnualValue;
    private BigDecimal currentAnnualValue; // Adjusted based on performance
    private BigDecimal totalValue;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer contractYears;
    
    @Enumerated(EnumType.STRING)
    private SponsorshipStatus status;
    
    // Performance bonuses
    private BigDecimal leaguePositionBonus;
    private BigDecimal cupProgressBonus;
    private BigDecimal attendanceBonus;
    private BigDecimal reputationBonus;
    
    // Contract terms
    private String contractTerms;
    private Boolean autoRenewal;
    private Integer renewalYears;
    private BigDecimal renewalBonusPercentage;
    
    // Performance tracking
    private Integer currentLeaguePosition;
    private Integer bestLeaguePosition;
    private Integer cupRoundsReached;
    private Double averageAttendance;
    private Integer reputationScore;
    
    @OneToMany(mappedBy = "sponsorshipDeal", cascade = CascadeType.ALL)
    private List<SponsorshipPayment> payments = new ArrayList<>();
    
    @OneToMany(mappedBy = "sponsorshipDeal", cascade = CascadeType.ALL)
    private List<SponsorshipBonus> bonuses = new ArrayList<>();
}
```

#### New Entity: Sponsor
```java
@Entity
@Table(name = "sponsor")
public class Sponsor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String logo;
    private String website;
    private String industry;
    
    @Enumerated(EnumType.STRING)
    private SponsorTier tier; // PREMIUM, STANDARD, BUDGET
    
    private BigDecimal maxAnnualBudget;
    private BigDecimal minClubReputation;
    private Integer minLeagueLevel;
    
    // Sponsor preferences
    private Boolean prefersWinningTeams;
    private Boolean prefersYoungTeams;
    private Boolean prefersLocalTeams;
    private String preferredRegion;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<SponsorshipType> availableTypes = new HashSet<>();
    
    @OneToMany(mappedBy = "sponsor", cascade = CascadeType.ALL)
    private List<SponsorshipDeal> deals = new ArrayList<>();
    
    @OneToMany(mappedBy = "sponsor", cascade = CascadeType.ALL)
    private List<SponsorshipOffer> offers = new ArrayList<>();
    
    private Boolean isActive;
    private LocalDate createdDate;
}
```

#### New Entity: SponsorshipOffer
```java
@Entity
@Table(name = "sponsorship_offer")
public class SponsorshipOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id")
    private Sponsor sponsor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    @Enumerated(EnumType.STRING)
    private SponsorshipType type;
    
    private BigDecimal offeredAnnualValue;
    private Integer contractYears;
    
    // Performance bonuses offered
    private BigDecimal leaguePositionBonus;
    private BigDecimal cupProgressBonus;
    private BigDecimal attendanceBonus;
    
    @Enumerated(EnumType.STRING)
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    
    private LocalDate offerDate;
    private LocalDate expiryDate;
    
    private String terms;
    private String specialConditions;
    
    // Negotiation tracking
    private Integer negotiationRounds;
    private BigDecimal lastCounterOffer;
    private String rejectionReason;
}
```

#### New Entity: SponsorshipPayment
```java
@Entity
@Table(name = "sponsorship_payment")
public class SponsorshipPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsorship_deal_id")
    private SponsorshipDeal sponsorshipDeal;
    
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType; // BASE_PAYMENT, PERFORMANCE_BONUS, RENEWAL_BONUS
    
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, PAID, OVERDUE
    
    private String description;
    private String reference;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
}
```

#### New Entity: SponsorshipBonus
```java
@Entity
@Table(name = "sponsorship_bonus")
public class SponsorshipBonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsorship_deal_id")
    private SponsorshipDeal sponsorshipDeal;
    
    @Enumerated(EnumType.STRING)
    private BonusType bonusType;
    
    private String bonusCondition; // e.g., "Finish in top 3", "Reach cup final"
    private BigDecimal bonusAmount;
    private Boolean isAchieved;
    private LocalDate achievedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
    
    private String notes;
}
```

#### Enums to Create
```java
public enum SponsorTier {
    PREMIUM("Premium Sponsor", 5000000, 1000000),
    STANDARD("Standard Sponsor", 2000000, 250000),
    BUDGET("Budget Sponsor", 500000, 50000);
    
    private final String displayName;
    private final BigDecimal maxBudget;
    private final BigDecimal minBudget;
}

public enum OfferStatus {
    PENDING("Pending Review"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    NEGOTIATING("Under Negotiation");
    
    private final String displayName;
}

public enum PaymentType {
    BASE_PAYMENT("Base Payment"),
    PERFORMANCE_BONUS("Performance Bonus"),
    RENEWAL_BONUS("Renewal Bonus"),
    SIGNING_BONUS("Signing Bonus");
    
    private final String displayName;
}

public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    OVERDUE("Overdue");
    
    private final String displayName;
}

public enum BonusType {
    LEAGUE_POSITION("League Position"),
    CUP_PROGRESS("Cup Progress"),
    ATTENDANCE("Attendance"),
    REPUTATION("Reputation"),
    GOALS_SCORED("Goals Scored"),
    CLEAN_SHEETS("Clean Sheets");
    
    private final String displayName;
}
```

### Service Layer Implementation

#### SponsorshipService
```java
@Service
public class SponsorshipService {
    
    @Autowired
    private SponsorRepository sponsorRepository;
    
    @Autowired
    private SponsorshipDealRepository sponsorshipDealRepository;
    
    @Autowired
    private SponsorshipOfferRepository sponsorshipOfferRepository;
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private FinancialService financialService;
    
    /**
     * Generate sponsorship offers for a club based on performance and reputation
     */
    public List<SponsorshipOffer> generateSponsorshipOffers(Long clubId) {
        Club club = clubService.findById(clubId);
        List<SponsorshipOffer> offers = new ArrayList<>();
        
        // Calculate club attractiveness score
        ClubAttractiveness attractiveness = calculateClubAttractiveness(club);
        
        // Get available sponsors based on club criteria
        List<Sponsor> availableSponsors = getAvailableSponsors(club, attractiveness);
        
        for (Sponsor sponsor : availableSponsors) {
            for (SponsorshipType type : sponsor.getAvailableTypes()) {
                // Check if club already has this type of sponsorship
                if (!hasActiveSponsorshipOfType(club, type)) {
                    SponsorshipOffer offer = createSponsorshipOffer(sponsor, club, type, attractiveness);
                    offers.add(offer);
                }
            }
        }
        
        return sponsorshipOfferRepository.saveAll(offers);
    }
    
    /**
     * Calculate club attractiveness for sponsors
     */
    private ClubAttractiveness calculateClubAttractiveness(Club club) {
        ClubAttractiveness attractiveness = new ClubAttractiveness();
        
        // League position factor (1-20, lower is better)
        Ranking currentRanking = rankingService.getCurrentRanking(club);
        if (currentRanking != null) {
            attractiveness.setLeaguePositionScore(21 - currentRanking.getPosition()); // Invert position
        }
        
        // Stadium capacity factor
        Stadium stadium = club.getStadium();
        if (stadium != null) {
            attractiveness.setStadiumScore(Math.min(stadium.getCapacity() / 1000, 100)); // Max 100 points
        }
        
        // Financial stability factor
        Finance finance = club.getFinance();
        if (finance != null) {
            attractiveness.setFinancialScore(calculateFinancialStabilityScore(finance));
        }
        
        // Historical performance factor
        attractiveness.setHistoricalScore(calculateHistoricalPerformanceScore(club));
        
        // Fan base factor (based on average attendance)
        attractiveness.setFanBaseScore(calculateFanBaseScore(club));
        
        // Calculate overall attractiveness (0-100)
        double overallScore = (attractiveness.getLeaguePositionScore() * 0.3 +
                              attractiveness.getStadiumScore() * 0.2 +
                              attractiveness.getFinancialScore() * 0.2 +
                              attractiveness.getHistoricalScore() * 0.2 +
                              attractiveness.getFanBaseScore() * 0.1);
        
        attractiveness.setOverallScore(Math.min(overallScore, 100));
        
        return attractiveness;
    }
    
    /**
     * Create sponsorship offer based on sponsor and club attractiveness
     */
    private SponsorshipOffer createSponsorshipOffer(Sponsor sponsor, Club club, 
                                                   SponsorshipType type, 
                                                   ClubAttractiveness attractiveness) {
        
        // Calculate base offer amount based on sponsor tier and club attractiveness
        BigDecimal baseAmount = calculateBaseOfferAmount(sponsor, type, attractiveness);
        
        // Calculate performance bonuses
        BigDecimal leagueBonus = calculateLeaguePositionBonus(sponsor, attractiveness);
        BigDecimal cupBonus = calculateCupProgressBonus(sponsor, attractiveness);
        BigDecimal attendanceBonus = calculateAttendanceBonus(sponsor, attractiveness);
        
        // Determine contract length based on sponsor preferences and club stability
        int contractYears = determineContractLength(sponsor, attractiveness);
        
        SponsorshipOffer offer = SponsorshipOffer.builder()
            .sponsor(sponsor)
            .club(club)
            .type(type)
            .offeredAnnualValue(baseAmount)
            .contractYears(contractYears)
            .leaguePositionBonus(leagueBonus)
            .cupProgressBonus(cupBonus)
            .attendanceBonus(attendanceBonus)
            .status(OfferStatus.PENDING)
            .offerDate(LocalDate.now())
            .expiryDate(LocalDate.now().plusDays(14)) // 2 weeks to respond
            .terms(generateContractTerms(sponsor, type, baseAmount))
            .negotiationRounds(0)
            .build();
        
        return offer;
    }
    
    /**
     * Accept sponsorship offer
     */
    @Transactional
    public SponsorshipDeal acceptSponsorshipOffer(Long offerId) {
        SponsorshipOffer offer = sponsorshipOfferRepository.findById(offerId)
            .orElseThrow(() -> new EntityNotFoundException("Sponsorship offer not found"));
        
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalStateException("Offer is no longer available");
        }
        
        if (offer.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Offer has expired");
        }
        
        // Create sponsorship deal
        SponsorshipDeal deal = SponsorshipDeal.builder()
            .club(offer.getClub())
            .sponsor(offer.getSponsor())
            .type(offer.getType())
            .baseAnnualValue(offer.getOfferedAnnualValue())
            .currentAnnualValue(offer.getOfferedAnnualValue())
            .totalValue(offer.getOfferedAnnualValue().multiply(BigDecimal.valueOf(offer.getContractYears())))
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(offer.getContractYears()))
            .contractYears(offer.getContractYears())
            .status(SponsorshipStatus.ACTIVE)
            .leaguePositionBonus(offer.getLeaguePositionBonus())
            .cupProgressBonus(offer.getCupProgressBonus())
            .attendanceBonus(offer.getAttendanceBonus())
            .contractTerms(offer.getTerms())
            .autoRenewal(false) // Default to manual renewal
            .build();
        
        deal = sponsorshipDealRepository.save(deal);
        
        // Update offer status
        offer.setStatus(OfferStatus.ACCEPTED);
        sponsorshipOfferRepository.save(offer);
        
        // Reject other pending offers of the same type
        rejectCompetingOffers(offer.getClub(), offer.getType(), offerId);
        
        // Create initial payment schedule
        createPaymentSchedule(deal);
        
        // Process signing bonus if applicable
        processSigningBonus(deal);
        
        return deal;
    }
    
    /**
     * Process monthly sponsorship payments
     */
    @Scheduled(cron = "0 0 9 1 * *") // First day of month at 9 AM
    public void processMonthlyPayments() {
        LocalDate today = LocalDate.now();
        
        // Get all pending payments due today or overdue
        List<SponsorshipPayment> duePayments = sponsorshipPaymentRepository
            .findByStatusAndDueDateLessThanEqual(PaymentStatus.PENDING, today);
        
        for (SponsorshipPayment payment : duePayments) {
            processPayment(payment);
        }
        
        // Mark overdue payments
        List<SponsorshipPayment> overduePayments = sponsorshipPaymentRepository
            .findByStatusAndDueDateLessThan(PaymentStatus.PENDING, today.minusDays(7));
        
        for (SponsorshipPayment payment : overduePayments) {
            payment.setStatus(PaymentStatus.OVERDUE);
            sponsorshipPaymentRepository.save(payment);
        }
    }
    
    /**
     * Process individual sponsorship payment
     */
    private void processPayment(SponsorshipPayment payment) {
        try {
            // Create financial transaction
            financialService.processTransaction(
                payment.getSponsorshipDeal().getClub().getId(),
                CreateTransactionRequest.builder()
                    .type(TransactionType.INCOME)
                    .category(TransactionCategory.SPONSORSHIP)
                    .amount(payment.getAmount())
                    .description(payment.getDescription())
                    .reference("SPONSORSHIP_" + payment.getId())
                    .effectiveDate(payment.getDueDate())
                    .build()
            );
            
            // Update payment status
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidDate(LocalDate.now());
            sponsorshipPaymentRepository.save(payment);
            
        } catch (Exception e) {
            log.error("Failed to process sponsorship payment {}: {}", payment.getId(), e.getMessage());
        }
    }
    
    /**
     * Evaluate performance bonuses at end of season
     */
    @EventListener
    public void evaluateSeasonBonuses(SeasonEndEvent event) {
        Season season = event.getSeason();
        List<SponsorshipDeal> activeDeals = sponsorshipDealRepository
            .findByStatusAndEndDateAfter(SponsorshipStatus.ACTIVE, season.getEndDate());
        
        for (SponsorshipDeal deal : activeDeals) {
            evaluateDealBonuses(deal, season);
        }
    }
    
    /**
     * Evaluate bonuses for a specific deal
     */
    private void evaluateDealBonuses(SponsorshipDeal deal, Season season) {
        Club club = deal.getClub();
        
        // League position bonus
        if (deal.getLeaguePositionBonus() != null && deal.getLeaguePositionBonus().compareTo(BigDecimal.ZERO) > 0) {
            evaluateLeaguePositionBonus(deal, club, season);
        }
        
        // Cup progress bonus
        if (deal.getCupProgressBonus() != null && deal.getCupProgressBonus().compareTo(BigDecimal.ZERO) > 0) {
            evaluateCupProgressBonus(deal, club, season);
        }
        
        // Attendance bonus
        if (deal.getAttendanceBonus() != null && deal.getAttendanceBonus().compareTo(BigDecimal.ZERO) > 0) {
            evaluateAttendanceBonus(deal, club, season);
        }
    }
    
    /**
     * Get sponsorship dashboard data
     */
    public SponsorshipDashboardDTO getSponsorshipDashboard(Long clubId) {
        Club club = clubService.findById(clubId);
        
        // Get active deals
        List<SponsorshipDeal> activeDeals = sponsorshipDealRepository
            .findByClubAndStatus(club, SponsorshipStatus.ACTIVE);
        
        // Get pending offers
        List<SponsorshipOffer> pendingOffers = sponsorshipOfferRepository
            .findByClubAndStatusAndExpiryDateAfter(club, OfferStatus.PENDING, LocalDate.now());
        
        // Calculate total annual value
        BigDecimal totalAnnualValue = activeDeals.stream()
            .map(SponsorshipDeal::getCurrentAnnualValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get recent payments
        List<SponsorshipPayment> recentPayments = sponsorshipPaymentRepository
            .findTop10ByClubOrderByPaidDateDesc(club);
        
        return SponsorshipDashboardDTO.builder()
            .activeDeals(activeDeals.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .pendingOffers(pendingOffers.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .totalAnnualValue(totalAnnualValue)
            .totalActiveDeals(activeDeals.size())
            .recentPayments(recentPayments.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .clubAttractiveness(calculateClubAttractiveness(club))
            .build();
    }
    
    private BigDecimal calculateBaseOfferAmount(Sponsor sponsor, SponsorshipType type, 
                                              ClubAttractiveness attractiveness) {
        // Base amount based on sponsor tier and sponsorship type
        BigDecimal baseAmount = switch (sponsor.getTier()) {
            case PREMIUM -> switch (type) {
                case SHIRT -> BigDecimal.valueOf(2000000);
                case STADIUM -> BigDecimal.valueOf(5000000);
                case TRAINING_GROUND -> BigDecimal.valueOf(1000000);
                case GENERAL -> BigDecimal.valueOf(500000);
            };
            case STANDARD -> switch (type) {
                case SHIRT -> BigDecimal.valueOf(800000);
                case STADIUM -> BigDecimal.valueOf(2000000);
                case TRAINING_GROUND -> BigDecimal.valueOf(400000);
                case GENERAL -> BigDecimal.valueOf(200000);
            };
            case BUDGET -> switch (type) {
                case SHIRT -> BigDecimal.valueOf(200000);
                case STADIUM -> BigDecimal.valueOf(500000);
                case TRAINING_GROUND -> BigDecimal.valueOf(100000);
                case GENERAL -> BigDecimal.valueOf(50000);
            };
        };
        
        // Adjust based on club attractiveness (0.5x to 2.0x multiplier)
        double multiplier = 0.5 + (attractiveness.getOverallScore() / 100.0 * 1.5);
        
        return baseAmount.multiply(BigDecimal.valueOf(multiplier));
    }
}
```

### API Endpoints

#### SponsorshipController
```java
@RestController
@RequestMapping("/api/sponsorship")
public class SponsorshipController {
    
    @Autowired
    private SponsorshipService sponsorshipService;
    
    @GetMapping("/club/{clubId}/dashboard")
    public ResponseEntity<SponsorshipDashboardDTO> getSponsorshipDashboard(@PathVariable Long clubId) {
        SponsorshipDashboardDTO dashboard = sponsorshipService.getSponsorshipDashboard(clubId);
        return ResponseEntity.ok(dashboard);
    }
    
    @PostMapping("/club/{clubId}/generate-offers")
    public ResponseEntity<List<SponsorshipOfferDTO>> generateOffers(@PathVariable Long clubId) {
        List<SponsorshipOffer> offers = sponsorshipService.generateSponsorshipOffers(clubId);
        return ResponseEntity.ok(offers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/offer/{offerId}/accept")
    public ResponseEntity<SponsorshipDealDTO> acceptOffer(@PathVariable Long offerId) {
        SponsorshipDeal deal = sponsorshipService.acceptSponsorshipOffer(offerId);
        return ResponseEntity.ok(convertToDTO(deal));
    }
    
    @PostMapping("/offer/{offerId}/reject")
    public ResponseEntity<Void> rejectOffer(@PathVariable Long offerId) {
        sponsorshipService.rejectSponsorshipOffer(offerId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/offer/{offerId}/negotiate")
    public ResponseEntity<SponsorshipOfferDTO> negotiateOffer(
            @PathVariable Long offerId,
            @RequestBody NegotiationRequest request) {
        SponsorshipOffer offer = sponsorshipService.negotiateOffer(offerId, request);
        return ResponseEntity.ok(convertToDTO(offer));
    }
    
    @GetMapping("/club/{clubId}/deals")
    public ResponseEntity<List<SponsorshipDealDTO>> getActiveDeals(@PathVariable Long clubId) {
        List<SponsorshipDeal> deals = sponsorshipService.getActiveDeals(clubId);
        return ResponseEntity.ok(deals.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/club/{clubId}/offers")
    public ResponseEntity<List<SponsorshipOfferDTO>> getPendingOffers(@PathVariable Long clubId) {
        List<SponsorshipOffer> offers = sponsorshipService.getPendingOffers(clubId);
        return ResponseEntity.ok(offers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### SponsorshipDashboard Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getSponsorshipDashboard, generateOffers, acceptOffer, rejectOffer } from '../services/api';

const SponsorshipDashboard = ({ clubId }) => {
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [selectedOffer, setSelectedOffer] = useState(null);

    useEffect(() => {
        loadSponsorshipData();
    }, [clubId]);

    const loadSponsorshipData = async () => {
        try {
            const response = await getSponsorshipDashboard(clubId);
            setDashboard(response.data);
        } catch (error) {
            console.error('Error loading sponsorship data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateOffers = async () => {
        try {
            await generateOffers(clubId);
            loadSponsorshipData(); // Refresh data
        } catch (error) {
            console.error('Error generating offers:', error);
        }
    };

    const handleAcceptOffer = async (offerId) => {
        try {
            await acceptOffer(offerId);
            loadSponsorshipData(); // Refresh data
        } catch (error) {
            console.error('Error accepting offer:', error);
        }
    };

    const handleRejectOffer = async (offerId) => {
        try {
            await rejectOffer(offerId);
            loadSponsorshipData(); // Refresh data
        } catch (error) {
            console.error('Error rejecting offer:', error);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    };

    if (loading) return <div>Loading sponsorship dashboard...</div>;

    return (
        <div className="sponsorship-dashboard">
            <div className="dashboard-header">
                <h2>Sponsorship Management</h2>
                <button onClick={handleGenerateOffers} className="btn-primary">
                    Generate New Offers
                </button>
            </div>

            <div className="sponsorship-summary">
                <div className="summary-card">
                    <h3>Total Annual Value</h3>
                    <span className="amount">{formatCurrency(dashboard.totalAnnualValue)}</span>
                </div>
                <div className="summary-card">
                    <h3>Active Deals</h3>
                    <span className="count">{dashboard.totalActiveDeals}</span>
                </div>
                <div className="summary-card">
                    <h3>Club Attractiveness</h3>
                    <span className="score">{dashboard.clubAttractiveness.overallScore.toFixed(1)}/100</span>
                </div>
            </div>

            <div className="pending-offers">
                <h3>Pending Offers ({dashboard.pendingOffers.length})</h3>
                {dashboard.pendingOffers.length === 0 ? (
                    <p>No pending offers. Generate new offers to see available sponsorship deals.</p>
                ) : (
                    <div className="offers-list">
                        {dashboard.pendingOffers.map(offer => (
                            <div key={offer.id} className="offer-card">
                                <div className="offer-header">
                                    <div className="sponsor-info">
                                        <img src={offer.sponsor.logo} alt={offer.sponsor.name} />
                                        <div>
                                            <h4>{offer.sponsor.name}</h4>
                                            <span className="sponsor-tier">{offer.sponsor.tier}</span>
                                        </div>
                                    </div>
                                    <div className="offer-type">
                                        <span className="type-badge">{offer.type}</span>
                                    </div>
                                </div>
                                
                                <div className="offer-details">
                                    <div className="offer-value">
                                        <span className="label">Annual Value:</span>
                                        <span className="value">{formatCurrency(offer.offeredAnnualValue)}</span>
                                    </div>
                                    <div className="contract-length">
                                        <span className="label">Contract Length:</span>
                                        <span className="value">{offer.contractYears} years</span>
                                    </div>
                                    <div className="total-value">
                                        <span className="label">Total Value:</span>
                                        <span className="value">
                                            {formatCurrency(offer.offeredAnnualValue * offer.contractYears)}
                                        </span>
                                    </div>
                                </div>

                                {(offer.leaguePositionBonus > 0 || offer.cupProgressBonus > 0 || offer.attendanceBonus > 0) && (
                                    <div className="performance-bonuses">
                                        <h5>Performance Bonuses:</h5>
                                        {offer.leaguePositionBonus > 0 && (
                                            <div className="bonus">
                                                League Position: {formatCurrency(offer.leaguePositionBonus)}
                                            </div>
                                        )}
                                        {offer.cupProgressBonus > 0 && (
                                            <div className="bonus">
                                                Cup Progress: {formatCurrency(offer.cupProgressBonus)}
                                            </div>
                                        )}
                                        {offer.attendanceBonus > 0 && (
                                            <div className="bonus">
                                                Attendance: {formatCurrency(offer.attendanceBonus)}
                                            </div>
                                        )}
                                    </div>
                                )}

                                <div className="offer-actions">
                                    <button 
                                        onClick={() => handleAcceptOffer(offer.id)}
                                        className="btn-success"
                                    >
                                        Accept Offer
                                    </button>
                                    <button 
                                        onClick={() => handleRejectOffer(offer.id)}
                                        className="btn-danger"
                                    >
                                        Reject
                                    </button>
                                    <button 
                                        onClick={() => setSelectedOffer(offer)}
                                        className="btn-secondary"
                                    >
                                        View Details
                                    </button>
                                </div>

                                <div className="offer-expiry">
                                    Expires: {new Date(offer.expiryDate).toLocaleDateString()}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="active-deals">
                <h3>Active Sponsorship Deals ({dashboard.activeDeals.length})</h3>
                <div className="deals-list">
                    {dashboard.activeDeals.map(deal => (
                        <div key={deal.id} className="deal-card">
                            <div className="deal-header">
                                <div className="sponsor-info">
                                    <img src={deal.sponsor.logo} alt={deal.sponsor.name} />
                                    <div>
                                        <h4>{deal.sponsor.name}</h4>
                                        <span className="deal-type">{deal.type}</span>
                                    </div>
                                </div>
                                <div className="deal-status">
                                    <span className={`status-badge ${deal.status.toLowerCase()}`}>
                                        {deal.status}
                                    </span>
                                </div>
                            </div>
                            
                            <div className="deal-details">
                                <div className="deal-value">
                                    <span className="label">Annual Value:</span>
                                    <span className="value">{formatCurrency(deal.currentAnnualValue)}</span>
                                </div>
                                <div className="contract-period">
                                    <span className="label">Contract Period:</span>
                                    <span className="value">
                                        {new Date(deal.startDate).toLocaleDateString()} - 
                                        {new Date(deal.endDate).toLocaleDateString()}
                                    </span>
                                </div>
                            </div>

                            <div className="deal-progress">
                                <div className="progress-bar">
                                    <div 
                                        className="progress-fill"
                                        style={{ 
                                            width: `${calculateContractProgress(deal.startDate, deal.endDate)}%` 
                                        }}
                                    ></div>
                                </div>
                                <span className="progress-text">
                                    {calculateRemainingTime(deal.endDate)} remaining
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="club-attractiveness">
                <h3>Club Attractiveness Breakdown</h3>
                <div className="attractiveness-metrics">
                    <div className="metric">
                        <span className="metric-name">League Position</span>
                        <div className="metric-bar">
                            <div 
                                className="metric-fill"
                                style={{ width: `${dashboard.clubAttractiveness.leaguePositionScore}%` }}
                            ></div>
                        </div>
                        <span className="metric-value">{dashboard.clubAttractiveness.leaguePositionScore}/20</span>
                    </div>
                    <div className="metric">
                        <span className="metric-name">Stadium</span>
                        <div className="metric-bar">
                            <div 
                                className="metric-fill"
                                style={{ width: `${dashboard.clubAttractiveness.stadiumScore}%` }}
                            ></div>
                        </div>
                        <span className="metric-value">{dashboard.clubAttractiveness.stadiumScore}/100</span>
                    </div>
                    <div className="metric">
                        <span className="metric-name">Financial Stability</span>
                        <div className="metric-bar">
                            <div 
                                className="metric-fill"
                                style={{ width: `${dashboard.clubAttractiveness.financialScore}%` }}
                            ></div>
                        </div>
                        <span className="metric-value">{dashboard.clubAttractiveness.financialScore}/100</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

const calculateContractProgress = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const now = new Date();
    
    const total = end - start;
    const elapsed = now - start;
    
    return Math.min(Math.max((elapsed / total) * 100, 0), 100);
};

const calculateRemainingTime = (endDate) => {
    const end = new Date(endDate);
    const now = new Date();
    const diff = end - now;
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const months = Math.floor(days / 30);
    const years = Math.floor(months / 12);
    
    if (years > 0) return `${years} year${years > 1 ? 's' : ''}`;
    if (months > 0) return `${months} month${months > 1 ? 's' : ''}`;
    return `${days} day${days > 1 ? 's' : ''}`;
};

export default SponsorshipDashboard;
```

## Implementation Notes

1. **Dynamic Offers**: Sponsorship offers are generated based on club performance, reputation, and attractiveness
2. **Performance Bonuses**: Sponsors offer bonuses for league position, cup progress, and attendance
3. **Contract Management**: Full contract lifecycle from offer to expiry with automatic renewals
4. **Payment Processing**: Automated monthly payments with integration to financial system
5. **Negotiation System**: Basic negotiation mechanics for counter-offers
6. **Sponsor Tiers**: Different sponsor tiers with varying budgets and requirements

## Dependencies

- Financial Management System (for payment processing)
- Club and Stadium entities (for attractiveness calculation)
- Ranking system (for league position bonuses)
- Season system (for performance evaluation)
- Match system (for attendance tracking)

## Testing Strategy

1. **Unit Tests**: Test attractiveness calculation, offer generation logic
2. **Integration Tests**: Test payment processing, bonus evaluation
3. **Performance Tests**: Test with large numbers of sponsors and offers
4. **User Acceptance Tests**: Test complete sponsorship workflow from offer to payment