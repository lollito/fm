package com.lollito.fm.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Injury;
import com.lollito.fm.model.InjuryContext;
import com.lollito.fm.model.InjurySeverity;
import com.lollito.fm.model.InjuryStatus;
import com.lollito.fm.model.InjuryType;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.dto.CreateInjuryRequest;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.InjuryRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
@Slf4j
public class InjuryService {

    @Autowired
    private InjuryRepository injuryRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private StaffService staffService;

    @Value("${fm.injury.base-probability:0.02}")
    private Double baseProbability;

    @Value("${fm.injury.age-threshold:30}")
    private Integer ageThreshold;

    @Value("${fm.injury.condition-threshold:50}")
    private Integer conditionThreshold;

    public boolean checkForInjury(Player player, Double matchIntensity) {
        // Staff bonus
        double injuryReduction = 0.0;
        if (player.getTeam() != null) {
            Optional<Club> clubOpt = clubRepository.findByTeam(player.getTeam());
            if (clubOpt.isPresent()) {
                var bonuses = staffService.calculateClubStaffBonuses(clubOpt.get().getId());
                if (bonuses != null && bonuses.getInjuryPreventionBonus() != null) {
                    injuryReduction = bonuses.getInjuryPreventionBonus();
                }
            }
        }
        return checkForInjury(player, matchIntensity, injuryReduction);
    }

    public boolean checkForInjury(Player player, Double matchIntensity, Double injuryReduction) {
        // Age factor (older players more prone)
        double ageFactor = player.getAge() > ageThreshold ? 1.5 : 1.0;

        // Condition factor (tired players more prone)
        double conditionFactor = player.getCondition() < conditionThreshold ? 2.0 : 1.0;

        // Previous injury factor
        boolean hasHistory = !player.getInjuries().isEmpty();
        double injuryHistoryFactor = hasHistory ? 1.3 : 1.0;

        double finalProbability = baseProbability * ageFactor *
                                conditionFactor * injuryHistoryFactor *
                                matchIntensity * (1.0 - (injuryReduction != null ? injuryReduction : 0.0));

        // Use ThreadLocalRandom for correct probability [0.0, 1.0)
        return ThreadLocalRandom.current().nextDouble() < finalProbability;
    }

    public Injury createInjury(Player player, InjuryContext context) {
        InjuryType type = determineInjuryType(context);
        InjurySeverity severity = determineInjurySeverity(player, type);

        int minDays = severity.getMinDays();
        int maxDays = severity.getMaxDays();
        int recoveryDays = RandomUtils.randomValue(minDays, maxDays);

        Injury injury = Injury.builder()
            .player(player)
            .type(type)
            .severity(severity)
            .injuryDate(LocalDate.now())
            .expectedRecoveryDate(LocalDate.now().plusDays(recoveryDays))
            .status(InjuryStatus.ACTIVE)
            .performanceImpact(calculatePerformanceImpact(severity))
            .description(generateInjuryDescription(type, severity))
            .build();

        return injuryRepository.save(injury);
    }

    public Injury createManualInjury(Long playerId, CreateInjuryRequest request) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));

        InjuryType type = request.getType();
        InjurySeverity severity = request.getSeverity();

        int recoveryDays;
        if (request.getDurationDays() != null) {
            recoveryDays = request.getDurationDays();
        } else {
             recoveryDays = RandomUtils.randomValue(severity.getMinDays(), severity.getMaxDays());
        }

        Injury injury = Injury.builder()
            .player(player)
            .type(type)
            .severity(severity)
            .injuryDate(LocalDate.now())
            .expectedRecoveryDate(LocalDate.now().plusDays(recoveryDays))
            .status(InjuryStatus.ACTIVE)
            .performanceImpact(calculatePerformanceImpact(severity))
            .description(generateInjuryDescription(type, severity))
            .build();

        return injuryRepository.save(injury);
    }

    @Scheduled(initialDelayString = "${fm.scheduling.injury.initial-delay}", fixedRateString = "${fm.scheduling.injury.fixed-rate}")
    public void processInjuryRecovery() {
        log.info("Starting processInjuryRecovery...");
        List<Injury> activeInjuries = injuryRepository.findByStatus(InjuryStatus.ACTIVE);

        for (Injury injury : activeInjuries) {
            if (LocalDate.now().isAfter(injury.getExpectedRecoveryDate()) || LocalDate.now().isEqual(injury.getExpectedRecoveryDate())) {
                // Check for recovery with some randomness (80% chance)
                if (RandomUtils.randomPercentage(80)) {
                    injury.setStatus(InjuryStatus.HEALED);
                    injury.setActualRecoveryDate(LocalDate.now());
                    injuryRepository.save(injury);

                    log.info("Player {} {} recovered from {}", injury.getPlayer().getName(), injury.getPlayer().getSurname(), injury.getType());
                }
            }
        }
        log.info("Finished processInjuryRecovery.");
    }

    public List<Injury> getTeamInjuries(Long teamId) {
        return injuryRepository.findByPlayerTeamIdAndStatus(teamId, InjuryStatus.ACTIVE);
    }

    public List<Injury> getClubInjuries(Long clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() -> new RuntimeException("Club not found"));
        if (club.getTeam() == null) return List.of();
        return getTeamInjuries(club.getTeam().getId());
    }

    public List<Injury> getPlayerInjuryHistory(Long playerId) {
        return injuryRepository.findByPlayerId(playerId);
    }

    private InjuryType determineInjuryType(InjuryContext context) {
        InjuryType[] types = InjuryType.values();
        return types[RandomUtils.randomValue(0, types.length - 1)];
    }

    private InjurySeverity determineInjurySeverity(Player player, InjuryType type) {
         int roll = RandomUtils.randomValue(1, 100);
         if (roll < 50) return InjurySeverity.MINOR;
         if (roll < 80) return InjurySeverity.MODERATE;
         if (roll < 95) return InjurySeverity.MAJOR;
         return InjurySeverity.SEVERE;
    }

    private Double calculatePerformanceImpact(InjurySeverity severity) {
        double val = 1.0;
        switch (severity) {
            case MINOR: // 0.85 - 0.95
                val = 0.85 + ThreadLocalRandom.current().nextDouble() * 0.10;
                break;
            case MODERATE: // 0.70 - 0.85
                 val = 0.70 + ThreadLocalRandom.current().nextDouble() * 0.15;
                 break;
            case MAJOR: // 0.50 - 0.70
                 val = 0.50 + ThreadLocalRandom.current().nextDouble() * 0.20;
                 break;
            case SEVERE: // 0.20 - 0.50
                 val = 0.20 + ThreadLocalRandom.current().nextDouble() * 0.30;
                 break;
        }
        return val;
    }

    private String generateInjuryDescription(InjuryType type, InjurySeverity severity) {
        return String.format("%s %s", severity, type);
    }
}
