package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.dto.AddToWatchlistRequest;
import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.dto.ScoutingRecommendationDTO;
import com.lollito.fm.model.AssignmentStatus;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerScoutingStatus;
import com.lollito.fm.model.RecommendationLevel;
import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.Scout;
import com.lollito.fm.model.ScoutSpecialization;
import com.lollito.fm.model.ScoutStatus;
import com.lollito.fm.model.ScoutingAssignment;
import com.lollito.fm.model.ScoutingLevel;
import com.lollito.fm.model.ScoutingReport;
import com.lollito.fm.model.ScoutingType;
import com.lollito.fm.repository.PlayerScoutingStatusRepository;
import com.lollito.fm.repository.ScoutRepository;
import com.lollito.fm.repository.ScoutingAssignmentRepository;
import com.lollito.fm.repository.ScoutingReportRepository;
import com.lollito.fm.utils.RandomUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
@Slf4j
public class ScoutingService {

    @Autowired private ScoutRepository scoutRepository;
    @Autowired private ScoutingAssignmentRepository assignmentRepository;
    @Autowired private ScoutingReportRepository reportRepository;
    @Autowired private PlayerScoutingStatusRepository scoutingStatusRepository;
    @Autowired private PlayerService playerService;
    @Autowired private ClubService clubService;
    @Autowired private WatchlistService watchlistService;
    @Autowired private ManagerProgressionService managerProgressionService;

    public List<Scout> getClubScouts(Long clubId) {
        Club club = clubService.findById(clubId);
        return scoutRepository.findByClub(club);
    }

    public List<ScoutingAssignment> getClubAssignments(Long clubId, AssignmentStatus status) {
        if (status != null) {
            return assignmentRepository.findByClubIdAndStatus(clubId, status);
        }
        return assignmentRepository.findByClubId(clubId);
    }

    public Page<ScoutingReport> getScoutingReports(Long clubId, Pageable pageable, RecommendationLevel recommendation) {
        if (recommendation != null) {
            return reportRepository.findByScout_Club_IdAndRecommendation(clubId, recommendation, pageable);
        }
        return reportRepository.findByScout_Club_Id(clubId, pageable);
    }

    @Transactional
    public ScoutingAssignment assignPlayerScouting(Long scoutId, Long playerId, Integer priority, String instructions) {
        Scout scout = scoutRepository.findById(scoutId)
            .orElseThrow(() -> new EntityNotFoundException("Scout not found"));
        Player player = playerService.findOne(playerId);

        if (!isScoutAvailable(scout)) {
            throw new IllegalStateException("Scout is not available for new assignments");
        }

        Optional<ScoutingAssignment> existingAssignment = assignmentRepository
            .findActiveAssignmentForPlayer(scout.getClub(), player);

        if (existingAssignment.isPresent()) {
            throw new IllegalStateException("Player is already being scouted");
        }

        int daysToComplete = calculateScoutingDuration(scout, player, ScoutingType.PLAYER);

        if (scout.getClub() != null && scout.getClub().getUser() != null) {
            if (managerProgressionService.hasPerk(scout.getClub().getUser(), ManagerPerk.HAWK_EYE)) {
                daysToComplete = (int) (daysToComplete * 0.9);
            }
        }

        ScoutingAssignment assignment = ScoutingAssignment.builder()
            .scout(scout)
            .targetPlayer(player)
            .type(ScoutingType.PLAYER)
            .status(AssignmentStatus.ASSIGNED)
            .assignedDate(LocalDate.now())
            .expectedCompletionDate(LocalDate.now().plusDays(daysToComplete))
            .priority(priority)
            .instructions(instructions)
            .build();

        assignment = assignmentRepository.save(assignment);

        // Auto-start
        assignment.setStatus(AssignmentStatus.IN_PROGRESS);
        assignment = assignmentRepository.save(assignment);

        return assignment;
    }

    private boolean isScoutAvailable(Scout scout) {
        return scout.getStatus() == ScoutStatus.ACTIVE;
    }

    @Scheduled(initialDelayString = "${fm.scheduling.scouting.initial-delay}", fixedRateString = "${fm.scheduling.scouting.fixed-rate}")
    @Transactional
    public void processDailyScoutingProgress() {
        log.info("Starting processDailyScoutingProgress...");
        List<ScoutingAssignment> activeAssignments = assignmentRepository
            .findAllWithScoutAndPlayerAndClub(AssignmentStatus.IN_PROGRESS);

        for (ScoutingAssignment assignment : activeAssignments) {
            processScoutingProgress(assignment);
        }
        log.info("Finished processDailyScoutingProgress.");
    }

    private void processScoutingProgress(ScoutingAssignment assignment) {
        Scout scout = assignment.getScout();

        if (scout.getStatus() != ScoutStatus.ACTIVE) {
            return;
        }

        if (LocalDate.now().isAfter(assignment.getExpectedCompletionDate()) || LocalDate.now().isEqual(assignment.getExpectedCompletionDate())) {
            completeScoutingAssignment(assignment);
        } else {
             if (RandomUtils.randomPercentage(5.0)) {
                 completeScoutingAssignment(assignment);
             }
        }
    }

    private void completeScoutingAssignment(ScoutingAssignment assignment) {
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setCompletionDate(LocalDate.now());

        ScoutingReport report = generateScoutingReport(assignment);

        updatePlayerScoutingStatus(assignment.getTargetPlayer(),
                                 assignment.getScout().getClub(), report);

        assignmentRepository.save(assignment);
    }

    public ScoutingReport generateScoutingReport(ScoutingAssignment assignment) {
        Scout scout = assignment.getScout();
        Player player = assignment.getTargetPlayer();

        double baseAccuracy = scout.getAbility() / 20.0;
        double experienceBonus = Math.min(0.2, scout.getExperience() / 100.0);
        double specializationBonus = isPlayerInScoutSpecialization(player, scout) ? 0.1 : 0.0;

        double totalAccuracy = Math.min(0.95, baseAccuracy + experienceBonus + specializationBonus);

        ScoutingReport report = ScoutingReport.builder()
            .assignment(assignment)
            .player(player)
            .scout(scout)
            .reportDate(LocalDate.now())
            .accuracyLevel(totalAccuracy)
            .confidenceLevel(calculateConfidenceLevel(scout, totalAccuracy))
            .build();

        revealPlayerAttributes(report, player, totalAccuracy);
        generatePlayerAssessment(report, player, totalAccuracy);
        generateMarketAssessment(report, player, totalAccuracy);

        return reportRepository.save(report);
    }

    private boolean isPlayerInScoutSpecialization(Player player, Scout scout) {
        if (scout.getSpecialization() == ScoutSpecialization.GENERAL) return true;

        if (player.getRole() == null) return false;

        switch(player.getRole()) {
            case GOALKEEPER: return scout.getSpecialization() == ScoutSpecialization.GOALKEEPERS;
            case DEFENDER: return scout.getSpecialization() == ScoutSpecialization.DEFENDERS;
            case MIDFIELDER: return scout.getSpecialization() == ScoutSpecialization.MIDFIELDERS;
            case WINGBACK: return scout.getSpecialization() == ScoutSpecialization.DEFENDERS || scout.getSpecialization() == ScoutSpecialization.MIDFIELDERS;
            case WING: return scout.getSpecialization() == ScoutSpecialization.MIDFIELDERS || scout.getSpecialization() == ScoutSpecialization.FORWARDS;
            case FORWARD: return scout.getSpecialization() == ScoutSpecialization.FORWARDS;
            default: return false;
        }
    }

    private Integer calculateConfidenceLevel(Scout scout, double accuracy) {
        return (int) (accuracy * 10);
    }

    private void revealPlayerAttributes(ScoutingReport report, Player player, double accuracy) {
        double variance = (1.0 - accuracy) * 10.0;

        report.setRevealedStamina(addScoutingVariance(player.getStamina(), variance));
        report.setRevealedPlaymaking(addScoutingVariance(player.getPlaymaking(), variance));
        report.setRevealedScoring(addScoutingVariance(player.getScoring(), variance));
        report.setRevealedWinger(addScoutingVariance(player.getWinger(), variance));
        report.setRevealedGoalkeeping(addScoutingVariance(player.getGoalkeeping(), variance));
        report.setRevealedPassing(addScoutingVariance(player.getPassing(), variance));
        report.setRevealedDefending(addScoutingVariance(player.getDefending(), variance));
        report.setRevealedSetPieces(addScoutingVariance(player.getSetPieces(), variance));

        report.setOverallRating(calculateOverallRating(report));
        report.setPotentialRating(calculatePotentialRating(player, accuracy));
    }

    private double addScoutingVariance(Double actualValue, double variance) {
        if (actualValue == null) return 0.0;
        double randomVariance = RandomUtils.randomValue(-variance, variance);
        double result = actualValue + randomVariance;
        return Math.max(0.0, Math.min(99.0, result));
    }

    private Integer calculateOverallRating(ScoutingReport report) {
        if (report.getPlayer().getRole() == null) return calculateSimpleAverage(report);

        double weightedSum = 0;
        double totalWeight = 0;

        // Weights
        double staminaW = 1.0;
        double playmakingW = 1.0;
        double scoringW = 1.0;
        double wingerW = 1.0;
        double goalkeepingW = 1.0;
        double passingW = 1.0;
        double defendingW = 1.0;
        double setPiecesW = 1.0;

        switch (report.getPlayer().getRole()) {
            case GOALKEEPER:
                goalkeepingW = 5.0;
                defendingW = 2.0;
                setPiecesW = 1.5;
                break;
            case DEFENDER:
                defendingW = 4.0;
                staminaW = 2.0;
                playmakingW = 1.5;
                passingW = 1.5;
                break;
            case WINGBACK:
                defendingW = 3.0;
                wingerW = 3.0;
                staminaW = 2.5;
                passingW = 2.0;
                playmakingW = 1.5;
                break;
            case MIDFIELDER:
                playmakingW = 4.0;
                passingW = 3.5;
                defendingW = 2.5;
                staminaW = 2.5;
                scoringW = 2.0;
                break;
            case WING:
                wingerW = 4.0;
                playmakingW = 3.0;
                passingW = 2.5;
                staminaW = 2.0;
                scoringW = 2.0;
                break;
            case FORWARD:
                scoringW = 5.0;
                passingW = 2.0;
                playmakingW = 2.0;
                wingerW = 1.5;
                break;
        }

        weightedSum += (report.getRevealedStamina() != null ? report.getRevealedStamina() : 0) * staminaW;
        weightedSum += (report.getRevealedPlaymaking() != null ? report.getRevealedPlaymaking() : 0) * playmakingW;
        weightedSum += (report.getRevealedScoring() != null ? report.getRevealedScoring() : 0) * scoringW;
        weightedSum += (report.getRevealedWinger() != null ? report.getRevealedWinger() : 0) * wingerW;
        weightedSum += (report.getRevealedGoalkeeping() != null ? report.getRevealedGoalkeeping() : 0) * goalkeepingW;
        weightedSum += (report.getRevealedPassing() != null ? report.getRevealedPassing() : 0) * passingW;
        weightedSum += (report.getRevealedDefending() != null ? report.getRevealedDefending() : 0) * defendingW;
        weightedSum += (report.getRevealedSetPieces() != null ? report.getRevealedSetPieces() : 0) * setPiecesW;

        totalWeight = staminaW + playmakingW + scoringW + wingerW + goalkeepingW + passingW + defendingW + setPiecesW;

        return (int) (weightedSum / totalWeight);
    }

    private Integer calculateSimpleAverage(ScoutingReport report) {
        double sum = (report.getRevealedStamina() != null ? report.getRevealedStamina() : 0) +
                     (report.getRevealedPlaymaking() != null ? report.getRevealedPlaymaking() : 0) +
                     (report.getRevealedScoring() != null ? report.getRevealedScoring() : 0) +
                     (report.getRevealedWinger() != null ? report.getRevealedWinger() : 0) +
                     (report.getRevealedGoalkeeping() != null ? report.getRevealedGoalkeeping() : 0) +
                     (report.getRevealedPassing() != null ? report.getRevealedPassing() : 0) +
                     (report.getRevealedDefending() != null ? report.getRevealedDefending() : 0) +
                     (report.getRevealedSetPieces() != null ? report.getRevealedSetPieces() : 0);
        return (int) (sum / 8);
    }

    private Integer calculatePotentialRating(Player player, double accuracy) {
        Double actualPotential = player.getPotential();
        if (actualPotential == null) {
            // Fallback if potential not set (e.g. legacy data)
            actualPotential = player.getAverage() + (player.getAge() < 21 ? 20.0 : 5.0);
        }

        // Variance based on accuracy. Lower accuracy -> larger range.
        double variance = (1.0 - accuracy) * 20.0;
        return (int) addScoutingVariance(actualPotential, variance);
    }

    private void generatePlayerAssessment(ScoutingReport report, Player player, double accuracy) {
        java.util.List<String> strengths = new java.util.ArrayList<>();
        java.util.List<String> weaknesses = new java.util.ArrayList<>();

        // Analyze stats
        analyzeStat(report.getRevealedStamina(), "Stamina", strengths, weaknesses);
        analyzeStat(report.getRevealedPlaymaking(), "Playmaking", strengths, weaknesses);
        analyzeStat(report.getRevealedScoring(), "Scoring", strengths, weaknesses);
        analyzeStat(report.getRevealedWinger(), "Winger", strengths, weaknesses);
        analyzeStat(report.getRevealedGoalkeeping(), "Goalkeeping", strengths, weaknesses);
        analyzeStat(report.getRevealedPassing(), "Passing", strengths, weaknesses);
        analyzeStat(report.getRevealedDefending(), "Defending", strengths, weaknesses);
        analyzeStat(report.getRevealedSetPieces(), "Set Pieces", strengths, weaknesses);

        // Add hidden potential hint if scout is good enough
        if (accuracy > 0.8 && report.getPotentialRating() > report.getOverallRating() + 10) {
            strengths.add("Has significant room for improvement.");
        }

        report.setStrengths(String.join("|", strengths));
        report.setWeaknesses(String.join("|", weaknesses));
        report.setPersonalityAssessment("Professional."); // Placeholder for now or random
        report.setRecommendation(calculateRecommendation(report, player));
    }

    private void analyzeStat(Double value, String name, java.util.List<String> strengths, java.util.List<String> weaknesses) {
        if (value == null) return;
        if (value >= 80) strengths.add("Excellent " + name);
        else if (value >= 60) strengths.add("Good " + name);
        else if (value <= 20) weaknesses.add("Poor " + name);
        else if (value <= 40) weaknesses.add("Weak " + name);
    }

    private void generateMarketAssessment(ScoutingReport report, Player player, double accuracy) {
        report.setEstimatedValue(new BigDecimal(player.getAverage() * 100000));
        report.setEstimatedWage(new BigDecimal(player.getAverage() * 1000));
        report.setIsAvailableForTransfer(player.getOnSale());
    }

    private RecommendationLevel calculateRecommendation(ScoutingReport report, Player player) {
        int overallRating = report.getOverallRating();
        int potentialRating = report.getPotentialRating();
        int playerAge = player.getAge();

        if (playerAge < 23 && potentialRating > 85) return RecommendationLevel.PRIORITY;
        if (overallRating > 80) return RecommendationLevel.RECOMMEND;
        if (overallRating > 65) return RecommendationLevel.CONSIDER;
        if (overallRating > 50 || (playerAge < 25 && potentialRating > 70)) return RecommendationLevel.MONITOR;
        return RecommendationLevel.AVOID;
    }

    private void updatePlayerScoutingStatus(Player player, Club club, ScoutingReport report) {
        PlayerScoutingStatus status = scoutingStatusRepository.findByPlayerAndScoutingClub(player, club)
            .orElse(PlayerScoutingStatus.builder()
                .player(player)
                .scoutingClub(club)
                .scoutingLevel(ScoutingLevel.UNKNOWN)
                .timesScoutedThisSeason(0)
                .firstScoutedDate(LocalDate.now())
                .build());

        status.setLastScoutedDate(LocalDate.now());
        status.setTimesScoutedThisSeason(status.getTimesScoutedThisSeason() + 1);

        double currentAccuracy = status.getKnowledgeAccuracy() != null ? status.getKnowledgeAccuracy() : 0.0;
        double newAccuracy = Math.min(1.0, currentAccuracy + (report.getAccuracyLevel() * 0.2));
        status.setKnowledgeAccuracy(newAccuracy);

        if (newAccuracy > 0.8) status.setScoutingLevel(ScoutingLevel.COMPREHENSIVE);
        else if (newAccuracy > 0.5) status.setScoutingLevel(ScoutingLevel.DETAILED);
        else status.setScoutingLevel(ScoutingLevel.BASIC);

        status.setKnownStamina(report.getRevealedStamina());
        status.setKnownPlaymaking(report.getRevealedPlaymaking());
        status.setKnownScoring(report.getRevealedScoring());
        status.setKnownWinger(report.getRevealedWinger());
        status.setKnownGoalkeeping(report.getRevealedGoalkeeping());
        status.setKnownPassing(report.getRevealedPassing());
        status.setKnownDefending(report.getRevealedDefending());
        status.setKnownSetPieces(report.getRevealedSetPieces());

        scoutingStatusRepository.save(status);
    }

    public PlayerScoutingStatus getPlayerScoutingStatus(Long playerId, Long clubId) {
        Player player = playerService.findOne(playerId);
        Club club = clubService.findById(clubId);

        return scoutingStatusRepository.findByPlayerAndScoutingClub(player, club)
            .orElse(PlayerScoutingStatus.builder()
                    .player(player)
                    .scoutingClub(club)
                    .scoutingLevel(ScoutingLevel.UNKNOWN)
                    .build());
    }

    public PlayerDTO getRevealedPlayerInfo(Long playerId, Long clubId) {
        PlayerScoutingStatus status = getPlayerScoutingStatus(playerId, clubId);
        Player player = status.getPlayer();

        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setSurname(player.getSurname());
        dto.setBirth(player.getBirth());
        dto.setRole(player.getRole());
        dto.setPreferredFoot(player.getPreferredFoot());

        if (status.getScoutingLevel() == ScoutingLevel.UNKNOWN) {
             dto.setStamina(null);
             dto.setPlaymaking(null);
             dto.setScoring(null);
             dto.setWinger(null);
             dto.setGoalkeeping(null);
             dto.setPassing(null);
             dto.setDefending(null);
             dto.setSetPieces(null);
        } else {
            dto.setStamina(status.getKnownStamina());
            dto.setPlaymaking(status.getKnownPlaymaking());
            dto.setScoring(status.getKnownScoring());
            dto.setWinger(status.getKnownWinger());
            dto.setGoalkeeping(status.getKnownGoalkeeping());
            dto.setPassing(status.getKnownPassing());
            dto.setDefending(status.getKnownDefending());
            dto.setSetPieces(status.getKnownSetPieces());
        }

        return dto;
    }

    public List<ScoutingRecommendationDTO> getScoutingRecommendations(Long clubId) {
        Club club = clubService.findById(clubId);
        List<ScoutingReport> reports = reportRepository.findRecentRecommendedReports(club, LocalDate.now().minusMonths(3));

        return reports.stream().map(r -> {
            ScoutingRecommendationDTO dto = new ScoutingRecommendationDTO();
            dto.setPlayerId(r.getPlayer().getId());
            dto.setPlayerName(r.getPlayer().getName());
            dto.setPlayerSurname(r.getPlayer().getSurname());
            dto.setPlayerRole(r.getPlayer().getRole().name());
            dto.setPlayerAge(r.getPlayer().getAge());
            dto.setCurrentClubName("Unknown");
            dto.setOverallRating(r.getOverallRating());
            dto.setPotentialRating(r.getPotentialRating());
            dto.setRecommendation(r.getRecommendation());
            dto.setEstimatedValue(r.getEstimatedValue());
            return dto;
        }).collect(Collectors.toList());
    }

    public void addToWatchlist(Long reportId, String notes) {
        ScoutingReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Scouting report not found"));

        Player player = report.getPlayer();
        Club club = report.getScout().getClub();

        AddToWatchlistRequest request = new AddToWatchlistRequest();
        request.setNotes(notes);

        watchlistService.addPlayerToWatchlist(club.getId(), player.getId(), request);
    }

    public void cancelAssignment(Long assignmentId) {
        ScoutingAssignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignmentRepository.save(assignment);
    }

    private int calculateScoutingDuration(Scout scout, Player player, ScoutingType type) {
        int baseDays = switch (type) {
            case PLAYER -> 14;
            case CLUB -> 30;
            case REGION -> 60;
            case OPPOSITION -> 7;
        };

        double abilityMultiplier = 1.0 - (scout.getAbility() / 40.0);
        double difficultyMultiplier = 1.0 + (player.getAverage() / 200.0);

        return (int) Math.max(3, baseDays * abilityMultiplier * difficultyMultiplier);
    }

}
