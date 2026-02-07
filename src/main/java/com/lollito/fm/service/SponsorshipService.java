package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.event.SeasonEndEvent;
import com.lollito.fm.model.BonusType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.OfferStatus;
import com.lollito.fm.model.PaymentStatus;
import com.lollito.fm.model.PaymentType;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Sponsor;
import com.lollito.fm.model.SponsorshipBonus;
import com.lollito.fm.model.SponsorshipDeal;
import com.lollito.fm.model.SponsorshipOffer;
import com.lollito.fm.model.SponsorshipPayment;
import com.lollito.fm.model.SponsorshipStatus;
import com.lollito.fm.model.SponsorshipType;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.dto.ClubAttractivenessDTO;
import com.lollito.fm.model.dto.NegotiationRequest;
import com.lollito.fm.model.dto.SponsorDTO;
import com.lollito.fm.model.dto.SponsorshipDashboardDTO;
import com.lollito.fm.model.dto.SponsorshipDealDTO;
import com.lollito.fm.model.dto.SponsorshipOfferDTO;
import com.lollito.fm.model.dto.SponsorshipPaymentDTO;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.repository.rest.RankingRepository;
import com.lollito.fm.repository.rest.SponsorRepository;
import com.lollito.fm.repository.rest.SponsorshipDealRepository;
import com.lollito.fm.repository.rest.SponsorshipOfferRepository;
import com.lollito.fm.repository.rest.SponsorshipPaymentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Slf4j
public class SponsorshipService {

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private SponsorshipDealRepository sponsorshipDealRepository;

    @Autowired
    private SponsorshipOfferRepository sponsorshipOfferRepository;

    @Autowired
    private SponsorshipPaymentRepository sponsorshipPaymentRepository;

    @Autowired
    private ClubService clubService;

    @Autowired
    private FinancialService financialService;

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private SeasonService seasonService;

    /**
     * Generate sponsorship offers for a club based on performance and reputation
     */
    @Transactional
    public List<SponsorshipOffer> generateSponsorshipOffers(Long clubId) {
        Club club = clubService.findById(clubId);
        List<SponsorshipOffer> offers = new ArrayList<>();

        // Calculate club attractiveness score
        ClubAttractiveness attractiveness = calculateClubAttractiveness(club);

        // Get available sponsors based on club criteria
        List<Sponsor> availableSponsors = getAvailableSponsors(club, attractiveness);

        for (Sponsor sponsor : availableSponsors) {
            for (SponsorshipType type : sponsor.getAvailableTypes()) {
                // Check if club already has this type of sponsorship (active deal or pending offer)
                if (!hasActiveSponsorshipOfType(club, type) && !hasPendingOfferOfType(club, type)) {
                    SponsorshipOffer offer = createSponsorshipOffer(sponsor, club, type, attractiveness);
                    offers.add(offer);
                }
            }
        }

        return sponsorshipOfferRepository.saveAll(offers);
    }

    private boolean hasActiveSponsorshipOfType(Club club, SponsorshipType type) {
        return sponsorshipDealRepository.existsByClubAndTypeAndStatus(club, type, SponsorshipStatus.ACTIVE);
    }

    private boolean hasPendingOfferOfType(Club club, SponsorshipType type) {
        List<SponsorshipOffer> offers = sponsorshipOfferRepository.findByClubAndTypeAndStatus(club, type, OfferStatus.PENDING);
        return !offers.isEmpty();
    }

    private List<Sponsor> getAvailableSponsors(Club club, ClubAttractiveness attractiveness) {
        // Simple logic: return all sponsors for now, filter by simple criteria
        // In real impl, would filter by tier matching attractiveness, region, etc.
        return sponsorRepository.findAll().stream()
                .filter(s -> s.getMinClubReputation() == null || s.getMinClubReputation().doubleValue() <= attractiveness.getOverallScore())
                .limit(5) // Limit to 5 sponsors to avoid spam
                .collect(Collectors.toList());
    }

    /**
     * Calculate club attractiveness for sponsors
     */
    private ClubAttractiveness calculateClubAttractiveness(Club club) {
        ClubAttractiveness attractiveness = new ClubAttractiveness();

        // League position factor (1-20, lower is better)
        Integer position = getCurrentLeaguePosition(club);
        if (position != null) {
            attractiveness.setLeaguePositionScore((double) Math.max(0, 21 - position)); // Invert position
        } else {
             attractiveness.setLeaguePositionScore(10.0); // Mid table default
        }

        // Stadium capacity factor
        Stadium stadium = club.getStadium();
        if (stadium != null) {
            attractiveness.setStadiumScore(Math.min((double)stadium.getCapacity() / 1000.0, 100.0)); // Max 100 points
        } else {
            attractiveness.setStadiumScore(10.0);
        }

        // Financial stability factor
        Finance finance = club.getFinance();
        if (finance != null) {
            attractiveness.setFinancialScore(calculateFinancialStabilityScore(finance));
        } else {
            attractiveness.setFinancialScore(50.0);
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

        attractiveness.setOverallScore(Math.min(overallScore, 100.0));

        return attractiveness;
    }

    private Integer getCurrentLeaguePosition(Club club) {
        Season currentSeason = seasonService.getCurrentSeason();
        if (currentSeason == null) return null;
        Ranking ranking = rankingRepository.findByClubAndSeason(club, currentSeason);
        if (ranking == null) return null;

        // Calculate position
        List<Ranking> allRankings = rankingRepository.findAll().stream() // Ideally findBySeason
                .filter(r -> r.getSeason().getId().equals(currentSeason.getId()))
                .sorted((r1, r2) -> {
                    int p1 = r1.getPoints();
                    int p2 = r2.getPoints();
                    if (p1 != p2) return p2 - p1;
                    int gd1 = r1.getGoalsFor() - r1.getGoalAgainst();
                    int gd2 = r2.getGoalsFor() - r2.getGoalAgainst();
                    return gd2 - gd1;
                })
                .collect(Collectors.toList());

        return allRankings.indexOf(ranking) + 1;
    }

    private double calculateFinancialStabilityScore(Finance finance) {
        if (finance.getBalance().compareTo(BigDecimal.ZERO) < 0) return 0.0;
        return Math.min(finance.getBalance().doubleValue() / 1000000.0 * 10, 100.0);
    }

    private double calculateHistoricalPerformanceScore(Club club) {
        // Placeholder
        return 50.0;
    }

    private double calculateFanBaseScore(Club club) {
        // Placeholder
        return 50.0;
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
        BigDecimal leagueBonus = baseAmount.multiply(BigDecimal.valueOf(0.1)); // 10%
        BigDecimal cupBonus = baseAmount.multiply(BigDecimal.valueOf(0.05)); // 5%
        BigDecimal attendanceBonus = baseAmount.multiply(BigDecimal.valueOf(0.02)); // 2%

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

    private String generateContractTerms(Sponsor sponsor, SponsorshipType type, BigDecimal amount) {
        return "Standard " + type + " sponsorship agreement with " + sponsor.getName() + " for " + amount + " per year.";
    }

    private int determineContractLength(Sponsor sponsor, ClubAttractiveness attractiveness) {
        return 1 + (int)(Math.random() * 3); // 1-3 years
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

        // Create initial payment schedule (first payment)
        createPaymentSchedule(deal);

        return deal;
    }

    public void rejectSponsorshipOffer(Long offerId) {
        SponsorshipOffer offer = sponsorshipOfferRepository.findById(offerId)
            .orElseThrow(() -> new EntityNotFoundException("Sponsorship offer not found"));
        offer.setStatus(OfferStatus.REJECTED);
        sponsorshipOfferRepository.save(offer);
    }

    @Transactional
    public SponsorshipOffer negotiateOffer(Long offerId, NegotiationRequest request) {
        SponsorshipOffer offer = sponsorshipOfferRepository.findById(offerId)
            .orElseThrow(() -> new EntityNotFoundException("Sponsorship offer not found"));

        if (offer.getStatus() != OfferStatus.PENDING && offer.getStatus() != OfferStatus.NEGOTIATING) {
            throw new IllegalStateException("Offer cannot be negotiated");
        }

        // Simple negotiation logic
        if (offer.getNegotiationRounds() != null && offer.getNegotiationRounds() >= 3) {
            throw new IllegalStateException("Maximum negotiation rounds reached");
        }

        // If request is within 20% of original offer, accept
        BigDecimal maxAcceptable = offer.getOfferedAnnualValue().multiply(BigDecimal.valueOf(1.2));
        if (request.getRequestedAnnualValue() != null && request.getRequestedAnnualValue().compareTo(maxAcceptable) <= 0) {
             offer.setOfferedAnnualValue(request.getRequestedAnnualValue());
             if (request.getRequestedContractYears() != null) {
                 offer.setContractYears(request.getRequestedContractYears());
             }
        } else {
             // Counter with small increase (e.g. 5%)
             BigDecimal counter = offer.getOfferedAnnualValue().multiply(BigDecimal.valueOf(1.05));
             offer.setOfferedAnnualValue(counter);
             offer.setLastCounterOffer(counter);
        }

        offer.setNegotiationRounds((offer.getNegotiationRounds() == null ? 0 : offer.getNegotiationRounds()) + 1);
        offer.setStatus(OfferStatus.NEGOTIATING);

        return sponsorshipOfferRepository.save(offer);
    }

    private void rejectCompetingOffers(Club club, SponsorshipType type, Long acceptedOfferId) {
        List<SponsorshipOffer> competingOffers = sponsorshipOfferRepository.findByClubAndTypeAndStatusAndIdNot(club, type, OfferStatus.PENDING, acceptedOfferId);
        for (SponsorshipOffer offer : competingOffers) {
            offer.setStatus(OfferStatus.REJECTED);
            offer.setRejectionReason("Accepted competing offer");
            sponsorshipOfferRepository.save(offer);
        }
    }

    private void createPaymentSchedule(SponsorshipDeal deal) {
        // Create a payment for now (Signing or first month)
        // For simplicity, let's say 1/12th is paid now
        BigDecimal amount = deal.getCurrentAnnualValue().divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);

        SponsorshipPayment payment = SponsorshipPayment.builder()
            .sponsorshipDeal(deal)
            .paymentType(PaymentType.BASE_PAYMENT)
            .amount(amount)
            .dueDate(LocalDate.now())
            .status(PaymentStatus.PENDING)
            .description("Initial Payment")
            .season(seasonService.getCurrentSeason())
            .build();

        sponsorshipPaymentRepository.save(payment);
    }

    /**
     * Process monthly sponsorship payments
     */
    @Scheduled(initialDelayString = "${fm.scheduling.sponsorship.initial-delay}", fixedRateString = "${fm.scheduling.sponsorship.fixed-rate}")
    @Transactional
    public void processMonthlyPayments() {
        log.info("Starting processMonthlyPayments...");
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

        // Generate next month payments for active deals
        generateNextMonthPayments();
        log.info("Finished processMonthlyPayments.");
    }

    private void generateNextMonthPayments() {
        List<SponsorshipDeal> activeDeals = sponsorshipDealRepository.findAll().stream()
                .filter(d -> d.getStatus() == SponsorshipStatus.ACTIVE)
                .collect(Collectors.toList());

        for (SponsorshipDeal deal : activeDeals) {
             BigDecimal amount = deal.getCurrentAnnualValue().divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
             SponsorshipPayment payment = SponsorshipPayment.builder()
                .sponsorshipDeal(deal)
                .paymentType(PaymentType.BASE_PAYMENT)
                .amount(amount)
                .dueDate(LocalDate.now().plusMonths(1).withDayOfMonth(1))
                .status(PaymentStatus.PENDING)
                .description("Monthly Payment")
                .season(seasonService.getCurrentSeason())
                .build();
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
    @Transactional
    public void evaluateSeasonBonuses(SeasonEndEvent event) {
        Season season = event.getSeason();
        List<SponsorshipDeal> activeDeals = sponsorshipDealRepository
            .findByStatusAndEndDateAfter(SponsorshipStatus.ACTIVE, LocalDate.of(season.getEndYear(), 6, 30));

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
            Ranking ranking = rankingRepository.findByClubAndSeason(club, season);
            if (ranking != null) {
                 // Logic to check if position meets criteria (e.g. top 3)
                 // For now, let's just award it if they finished in top 5
                 int position = getCurrentLeaguePosition(club); // This might need to check specific season if logic changes
                 if (position <= 5) {
                     createBonusPayment(deal, deal.getLeaguePositionBonus(), "League Position Bonus", season);
                 }
            }
        }
    }

    private void createBonusPayment(SponsorshipDeal deal, BigDecimal amount, String description, Season season) {
        SponsorshipPayment payment = SponsorshipPayment.builder()
            .sponsorshipDeal(deal)
            .paymentType(PaymentType.PERFORMANCE_BONUS)
            .amount(amount)
            .dueDate(LocalDate.now())
            .status(PaymentStatus.PENDING)
            .description(description)
            .season(season)
            .build();
        sponsorshipPaymentRepository.save(payment);

        // Also record bonus
        SponsorshipBonus bonus = SponsorshipBonus.builder()
                .sponsorshipDeal(deal)
                .bonusType(BonusType.LEAGUE_POSITION)
                .bonusAmount(amount)
                .isAchieved(true)
                .achievedDate(LocalDate.now())
                .season(season)
                .build();
        // Repository for bonus not created? The plan said entities created but I didn't create SponsorshipBonusRepository.
        // It's cascaded from Deal usually, or I can add it to deal list.
        deal.getBonuses().add(bonus);
        sponsorshipDealRepository.save(deal);
    }

    public List<SponsorshipDeal> getActiveDeals(Long clubId) {
        Club club = clubService.findById(clubId);
        return sponsorshipDealRepository.findByClubAndStatus(club, SponsorshipStatus.ACTIVE);
    }

    public List<SponsorshipOffer> getPendingOffers(Long clubId) {
        Club club = clubService.findById(clubId);
        return sponsorshipOfferRepository.findByClubAndStatusAndExpiryDateAfter(club, OfferStatus.PENDING, LocalDate.now());
    }

    /**
     * Get sponsorship dashboard data
     */
    public SponsorshipDashboardDTO getSponsorshipDashboard(Long clubId) {
        Club club = clubService.findById(clubId);

        // Get active deals
        List<SponsorshipDeal> activeDeals = getActiveDeals(clubId);

        // Get pending offers
        List<SponsorshipOffer> pendingOffers = getPendingOffers(clubId);

        // Calculate total annual value
        BigDecimal totalAnnualValue = activeDeals.stream()
            .map(SponsorshipDeal::getCurrentAnnualValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get recent payments
        List<SponsorshipPayment> recentPayments = sponsorshipPaymentRepository
            .findTop10BySponsorshipDeal_ClubOrderByPaidDateDesc(club);

        ClubAttractiveness attractiveness = calculateClubAttractiveness(club);

        return SponsorshipDashboardDTO.builder()
            .activeDeals(activeDeals.stream().map(this::convertToDealDTO).collect(Collectors.toList()))
            .pendingOffers(pendingOffers.stream().map(this::convertToOfferDTO).collect(Collectors.toList()))
            .totalAnnualValue(totalAnnualValue)
            .totalActiveDeals(activeDeals.size())
            .recentPayments(recentPayments.stream().map(this::convertToPaymentDTO).collect(Collectors.toList()))
            .clubAttractiveness(convertToAttractivenessDTO(attractiveness))
            .build();
    }

    private BigDecimal calculateBaseOfferAmount(Sponsor sponsor, SponsorshipType type,
                                              ClubAttractiveness attractiveness) {
        // Base amount based on sponsor tier and sponsorship type
        BigDecimal baseAmount;
        if (sponsor.getTier() != null) {
            switch (sponsor.getTier()) {
                case PREMIUM:
                    baseAmount = switch (type) {
                        case SHIRT -> BigDecimal.valueOf(2000000);
                        case STADIUM -> BigDecimal.valueOf(5000000);
                        case TRAINING_GROUND -> BigDecimal.valueOf(1000000);
                        case GENERAL -> BigDecimal.valueOf(500000);
                    };
                    break;
                case STANDARD:
                     baseAmount = switch (type) {
                        case SHIRT -> BigDecimal.valueOf(800000);
                        case STADIUM -> BigDecimal.valueOf(2000000);
                        case TRAINING_GROUND -> BigDecimal.valueOf(400000);
                        case GENERAL -> BigDecimal.valueOf(200000);
                    };
                    break;
                case BUDGET:
                    baseAmount = switch (type) {
                        case SHIRT -> BigDecimal.valueOf(200000);
                        case STADIUM -> BigDecimal.valueOf(500000);
                        case TRAINING_GROUND -> BigDecimal.valueOf(100000);
                        case GENERAL -> BigDecimal.valueOf(50000);
                    };
                    break;
                default:
                    baseAmount = BigDecimal.valueOf(100000);
            }
        } else {
             baseAmount = BigDecimal.valueOf(100000);
        }

        // Adjust based on club attractiveness (0.5x to 2.0x multiplier)
        double multiplier = 0.5 + (attractiveness.getOverallScore() / 100.0 * 1.5);

        return baseAmount.multiply(BigDecimal.valueOf(multiplier));
    }

    // DTO Converters

    public SponsorshipDealDTO convertToDealDTO(SponsorshipDeal deal) {
        return SponsorshipDealDTO.builder()
                .id(deal.getId())
                .sponsor(convertToSponsorDTO(deal.getSponsor()))
                .type(deal.getType())
                .currentAnnualValue(deal.getCurrentAnnualValue())
                .totalValue(deal.getTotalValue())
                .startDate(deal.getStartDate())
                .endDate(deal.getEndDate())
                .status(deal.getStatus())
                .build();
    }

    public SponsorshipOfferDTO convertToOfferDTO(SponsorshipOffer offer) {
        return SponsorshipOfferDTO.builder()
                .id(offer.getId())
                .sponsor(convertToSponsorDTO(offer.getSponsor()))
                .type(offer.getType())
                .offeredAnnualValue(offer.getOfferedAnnualValue())
                .contractYears(offer.getContractYears())
                .leaguePositionBonus(offer.getLeaguePositionBonus())
                .cupProgressBonus(offer.getCupProgressBonus())
                .attendanceBonus(offer.getAttendanceBonus())
                .status(offer.getStatus())
                .offerDate(offer.getOfferDate())
                .expiryDate(offer.getExpiryDate())
                .terms(offer.getTerms())
                .build();
    }

    public SponsorshipPaymentDTO convertToPaymentDTO(SponsorshipPayment payment) {
        return SponsorshipPaymentDTO.builder()
                .id(payment.getId())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .reference(payment.getReference())
                .build();
    }

    public SponsorDTO convertToSponsorDTO(Sponsor sponsor) {
        return SponsorDTO.builder()
                .id(sponsor.getId())
                .name(sponsor.getName())
                .logo(sponsor.getLogo())
                .industry(sponsor.getIndustry())
                .tier(sponsor.getTier())
                .build();
    }

    private ClubAttractivenessDTO convertToAttractivenessDTO(ClubAttractiveness att) {
        return ClubAttractivenessDTO.builder()
                .leaguePositionScore(att.getLeaguePositionScore())
                .stadiumScore(att.getStadiumScore())
                .financialScore(att.getFinancialScore())
                .historicalScore(att.getHistoricalScore())
                .fanBaseScore(att.getFanBaseScore())
                .overallScore(att.getOverallScore())
                .build();
    }

    // Helper class
    private static class ClubAttractiveness {
        private Double leaguePositionScore = 0.0;
        private Double stadiumScore = 0.0;
        private Double financialScore = 0.0;
        private Double historicalScore = 0.0;
        private Double fanBaseScore = 0.0;
        private Double overallScore = 0.0;

        public Double getLeaguePositionScore() { return leaguePositionScore; }
        public void setLeaguePositionScore(Double leaguePositionScore) { this.leaguePositionScore = leaguePositionScore; }
        public Double getStadiumScore() { return stadiumScore; }
        public void setStadiumScore(Double stadiumScore) { this.stadiumScore = stadiumScore; }
        public Double getFinancialScore() { return financialScore; }
        public void setFinancialScore(Double financialScore) { this.financialScore = financialScore; }
        public Double getHistoricalScore() { return historicalScore; }
        public void setHistoricalScore(Double historicalScore) { this.historicalScore = historicalScore; }
        public Double getFanBaseScore() { return fanBaseScore; }
        public void setFanBaseScore(Double fanBaseScore) { this.fanBaseScore = fanBaseScore; }
        public Double getOverallScore() { return overallScore; }
        public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }
    }
}
