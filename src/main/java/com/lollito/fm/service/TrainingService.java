package com.lollito.fm.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerTrainingResult;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;
import com.lollito.fm.model.TrainingPerformance;
import com.lollito.fm.model.TrainingPlan;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.TrainingStatus;
import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.dto.StaffBonusesDTO;
import com.lollito.fm.model.dto.IndividualFocusDTO;
import com.lollito.fm.model.dto.IndividualFocusRequest;
import com.lollito.fm.model.dto.ManualTrainingRequest;
import com.lollito.fm.model.dto.TrainingPlanRequest;
import com.lollito.fm.model.IndividualTrainingFocus;
import com.lollito.fm.model.PlayerTrainingFocus;
import com.lollito.fm.repository.PlayerTrainingFocusRepository;
import com.lollito.fm.repository.PlayerTrainingResultRepository;
import com.lollito.fm.repository.TrainingPlanRepository;
import com.lollito.fm.repository.TrainingSessionRepository;
import com.lollito.fm.repository.rest.ClubRepository;

@Service
@Slf4j
public class TrainingService {

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private PlayerTrainingResultRepository playerTrainingResultRepository;

    @Autowired
    private PlayerTrainingFocusRepository playerTrainingFocusRepository;

    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ManagerProgressionService managerProgressionService;

    /**
     * Process daily training for all teams
     */
    @Scheduled(initialDelayString = "${fm.scheduling.training.initial-delay}", fixedRateString = "${fm.scheduling.training.fixed-rate}")
    public void processDailyTraining() {
        log.info("Starting daily training processing");
        List<TrainingPlan> activePlans = trainingPlanRepository.findAll();

        for (TrainingPlan plan : activePlans) {
            try {
                if (shouldTrainToday(plan)) {
                    TrainingFocus todaysFocus = getTodaysFocus(plan);
                    if (todaysFocus != null) {
                        processTeamTraining(plan.getTeam(), todaysFocus, plan.getIntensity());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing training for team {}", plan.getTeam().getId(), e);
            }
        }
        log.info("Completed daily training processing");
    }

    private TrainingFocus getTodaysFocus(TrainingPlan plan) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return switch (today) {
            case MONDAY -> plan.getMondayFocus();
            case TUESDAY -> plan.getTuesdayFocus();
            case WEDNESDAY -> plan.getWednesdayFocus();
            case THURSDAY -> plan.getThursdayFocus();
            case FRIDAY -> plan.getFridayFocus();
            case SATURDAY -> plan.getSaturdayFocus();
            case SUNDAY -> plan.getSundayFocus();
        };
    }

    private boolean shouldTrainToday(TrainingPlan plan) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        if (Boolean.TRUE.equals(plan.getRestOnWeekends()) &&
            (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY)) {
            return false;
        }

        return getTodaysFocus(plan) != null;
    }

    /**
     * Process training session for a team
     */
    public TrainingSession processTeamTraining(Team team, TrainingFocus focus,
                                             TrainingIntensity intensity) {
        double effectiveness = calculateEffectiveness(team, focus);

        // Calculate applied bonus for history
        double appliedBonus = 0.0;
        Optional<Club> clubOpt = clubRepository.findByTeam(team);
        if (clubOpt.isPresent()) {
            StaffBonusesDTO bonuses = staffService.calculateClubStaffBonuses(clubOpt.get().getId());
            appliedBonus = getBonusForFocus(bonuses, focus);
        }

        TrainingSession session = TrainingSession.builder()
            .team(team)
            .focus(focus)
            .intensity(intensity)
            .startDate(LocalDate.now())
            .status(TrainingStatus.ACTIVE)
            .effectivenessMultiplier(effectiveness)
            .appliedBonus(appliedBonus)
            .build();

        session = trainingSessionRepository.save(session);

        // Fetch active individual focuses for the team
        List<Player> players = team.getPlayers();
        LocalDate today = LocalDate.now();
        List<PlayerTrainingFocus> activeFocuses = playerTrainingFocusRepository.findActiveFocusForPlayers(players, today);

        List<Player> playersToSave = new ArrayList<>();

        // Process each player's training
        for (Player player : players) {
            if (!player.isInjured() && player.getCondition() > 20) {
                PlayerTrainingFocus individualFocus = activeFocuses.stream()
                    .filter(f -> f.getPlayer().equals(player))
                    .findFirst()
                    .orElse(null);

                PlayerTrainingResult result = processPlayerTraining(player, session, individualFocus);
                session.getPlayerResults().add(result);
                playersToSave.add(player);
            }
        }

        // Batch save all updated players
        if (!playersToSave.isEmpty()) {
            playerService.saveAll(playersToSave);
        }

        session.setStatus(TrainingStatus.COMPLETED);
        session.setEndDate(LocalDate.now());

        // Session save will cascade save the new results added to playerResults
        return trainingSessionRepository.save(session);
    }

    /**
     * Calculate training effectiveness based on facilities and staff
     */
    public Double calculateEffectiveness(Team team, TrainingFocus focus) {
        double baseEffectiveness = 1.0;

        Optional<Club> clubOpt = clubRepository.findByTeam(team);
        if (clubOpt.isPresent()) {
            Club club = clubOpt.get();

            // Add facility bonuses
            if (club.getTrainingFacility() != null && club.getTrainingFacility().getOverallQuality() != null) {
                baseEffectiveness += club.getTrainingFacility().getOverallQuality() * 0.05;
            }

            // Add staff bonuses
            var bonuses = staffService.calculateClubStaffBonuses(club.getId());
            baseEffectiveness += getBonusForFocus(bonuses, focus);
        }

        return Math.min(2.0, baseEffectiveness); // Cap at 2x effectiveness
    }

    private Double getBonusForFocus(StaffBonusesDTO bonuses, TrainingFocus focus) {
        if (bonuses == null) return 0.0;

        Double bonus = switch (focus) {
            case ATTACKING -> bonuses.getAttackingBonus();
            case DEFENDING -> bonuses.getDefendingBonus();
            case PHYSICAL -> bonuses.getFitnessBonus();
            case TECHNICAL -> bonuses.getTacticalBonus();
            case GOALKEEPING -> bonuses.getGoalkeepingBonus();
            case BALANCED -> {
                double total = 0.0;
                total += bonuses.getAttackingBonus() != null ? bonuses.getAttackingBonus() : 0.0;
                total += bonuses.getDefendingBonus() != null ? bonuses.getDefendingBonus() : 0.0;
                total += bonuses.getFitnessBonus() != null ? bonuses.getFitnessBonus() : 0.0;
                total += bonuses.getTacticalBonus() != null ? bonuses.getTacticalBonus() : 0.0;
                yield total / 4.0;
            }
        };

        return bonus != null ? bonus : 0.0;
    }

    /**
     * Process individual player training
     */
    private PlayerTrainingResult processPlayerTraining(Player player,
                                                     TrainingSession session,
                                                     PlayerTrainingFocus individualFocus) {
        // Calculate attendance based on player condition and morale
        double attendanceRate = calculateAttendanceRate(player);

        // Calculate performance based on player attributes and randomness
        TrainingPerformance performance = calculateTrainingPerformance(player, session);

        // Calculate skill improvement
        double baseImprovement = 0.1; // Base skill points per session
        double improvement = baseImprovement *
                           session.getIntensity().getImprovementMultiplier() *
                           performance.getMultiplier() *
                           session.getEffectivenessMultiplier() *
                           attendanceRate;

        // Apply improvement to relevant skills (Team Focus)
        applySkillImprovement(player, session.getFocus(), improvement);

        // Calculate and apply fatigue (Team Focus)
        double fatigue = session.getIntensity().getFatigueMultiplier() *
                        attendanceRate * 10; // 10 condition points base

        // Apply Individual Focus if exists
        if (individualFocus != null) {
            // Extra improvement for specific attribute
            double individualImprovement = baseImprovement *
                                         individualFocus.getIntensity().getImprovementMultiplier() *
                                         performance.getMultiplier() *
                                         session.getEffectivenessMultiplier() *
                                         attendanceRate;

            applyIndividualSkillImprovement(player, individualFocus.getFocus(), individualImprovement);

            // Extra fatigue
            double individualFatigue = individualFocus.getIntensity().getFatigueMultiplier() *
                                     attendanceRate * 10;

            fatigue += individualFatigue;
            improvement += individualImprovement; // Total improvement gained tracking
        }

        // Handle negative fatigue (recovery)
        if (fatigue > 0) {
             player.decrementCondition(fatigue);
        } else {
             // If fatigue is negative, it means recovery
             player.incrementCondition(-fatigue);
        }

        // Player is not saved here anymore, collected for batch save in processTeamTraining

        // Create training result record
        // Result is returned transient and will be saved via cascade from session
        return PlayerTrainingResult.builder()
            .player(player)
            .trainingSession(session)
            .attendanceRate(attendanceRate)
            .improvementGained(improvement)
            .fatigueGained(fatigue)
            .performance(performance)
            .build();
    }

    private double calculateAttendanceRate(Player player) {
        // Higher morale and condition -> better attendance (effort)
        double moraleFactor = player.getMoral() / 100.0;
        double conditionFactor = player.getCondition() / 100.0;

        // Base attendance 80% + up to 20% based on morale/condition
        return 0.8 + (0.1 * moraleFactor) + (0.1 * conditionFactor);
    }

    private TrainingPerformance calculateTrainingPerformance(Player player, TrainingSession session) {
        // Simple random performance for now, could be based on player traits/age
        double roll = Math.random();

        if (roll < 0.1) return TrainingPerformance.POOR;
        if (roll < 0.6) return TrainingPerformance.AVERAGE;
        if (roll < 0.9) return TrainingPerformance.GOOD;
        return TrainingPerformance.EXCELLENT;
    }

    private void applyIndividualSkillImprovement(Player player, IndividualTrainingFocus focus, double improvement) {
        switch (focus) {
            case SCORING -> player.setScoring(Math.min(99.0, player.getScoring() + improvement));
            case WINGER -> player.setWinger(Math.min(99.0, player.getWinger() + improvement));
            case PASSING -> player.setPassing(Math.min(99.0, player.getPassing() + improvement));
            case DEFENDING -> player.setDefending(Math.min(99.0, player.getDefending() + improvement));
            case PLAYMAKING -> player.setPlaymaking(Math.min(99.0, player.getPlaymaking() + improvement));
            case STAMINA -> player.setStamina(Math.min(99.0, player.getStamina() + improvement));
            case GOALKEEPING -> player.setGoalkeeping(Math.min(99.0, player.getGoalkeeping() + improvement));
            case SET_PIECES -> player.setSetPieces(Math.min(99.0, player.getSetPieces() + improvement));
        }
    }

    /**
     * Apply skill improvements based on training focus
     */
    private void applySkillImprovement(Player player, TrainingFocus focus,
                                     double improvement) {
        if (focus == TrainingFocus.TECHNICAL || focus == TrainingFocus.BALANCED) {
            if (player.getTeam() != null && player.getTeam().getClub() != null && player.getTeam().getClub().getUser() != null) {
                if (managerProgressionService.hasPerk(player.getTeam().getClub().getUser(), ManagerPerk.VIDEO_ANALYST)) {
                    improvement *= 1.05;
                }
            }
        }

        List<String> affectedSkills = focus.getAffectedSkills();
        double improvementPerSkill = improvement / affectedSkills.size();

        for (String skill : affectedSkills) {
            switch (skill) {
                case "scoring" -> player.setScoring(
                    Math.min(99.0, player.getScoring() + improvementPerSkill));
                case "winger" -> player.setWinger(
                    Math.min(99.0, player.getWinger() + improvementPerSkill));
                case "passing" -> player.setPassing(
                    Math.min(99.0, player.getPassing() + improvementPerSkill));
                case "defending" -> player.setDefending(
                    Math.min(99.0, player.getDefending() + improvementPerSkill));
                case "playmaking" -> player.setPlaymaking(
                    Math.min(99.0, player.getPlaymaking() + improvementPerSkill));
                case "stamina" -> player.setStamina(
                    Math.min(99.0, player.getStamina() + improvementPerSkill));
                case "goalkeeping" -> player.setGoalkeeping(
                    Math.min(99.0, player.getGoalkeeping() + improvementPerSkill));
                case "setPieces" -> player.setSetPieces(
                    Math.min(99.0, player.getSetPieces() + improvementPerSkill));
            }
        }
    }

    public TrainingPlan updateTrainingPlan(Long teamId, TrainingPlanRequest request) {
        Team team = teamService.findById(teamId);

        TrainingPlan plan = team.getCurrentTrainingPlan();
        if (plan == null) {
            plan = new TrainingPlan();
            plan.setTeam(team);
        }

        plan.setMondayFocus(request.getMondayFocus());
        plan.setTuesdayFocus(request.getTuesdayFocus());
        plan.setWednesdayFocus(request.getWednesdayFocus());
        plan.setThursdayFocus(request.getThursdayFocus());
        plan.setFridayFocus(request.getFridayFocus());
        plan.setSaturdayFocus(request.getSaturdayFocus());
        plan.setSundayFocus(request.getSundayFocus());
        plan.setIntensity(request.getIntensity());
        plan.setRestOnWeekends(request.getRestOnWeekends());
        plan.setLastUpdated(LocalDateTime.now());

        return trainingPlanRepository.save(plan);
    }

    public TrainingPlan getTrainingPlan(Long teamId) {
         Team team = teamService.findById(teamId);
         if(team.getCurrentTrainingPlan() == null) {
             // Create default plan
             TrainingPlan plan = TrainingPlan.builder()
                .team(team)
                .intensity(TrainingIntensity.MODERATE)
                .mondayFocus(TrainingFocus.BALANCED)
                .tuesdayFocus(TrainingFocus.BALANCED)
                .wednesdayFocus(TrainingFocus.BALANCED)
                .thursdayFocus(TrainingFocus.BALANCED)
                .fridayFocus(TrainingFocus.BALANCED)
                .restOnWeekends(true)
                .lastUpdated(LocalDateTime.now())
                .build();
             return trainingPlanRepository.save(plan);
         }
         return team.getCurrentTrainingPlan();
    }

    @Transactional(readOnly = true)
    public Page<TrainingSession> getTrainingHistory(Long teamId, Pageable pageable) {
        Team team = teamService.findById(teamId);
        return trainingSessionRepository.findByTeam(team, pageable);
    }

    @Transactional(readOnly = true)
    public List<PlayerTrainingResult> getSessionResults(Long sessionId) {
        return trainingSessionRepository.findById(sessionId)
            .map(TrainingSession::getPlayerResults)
            .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public TrainingSession createManualTrainingSession(Long teamId, ManualTrainingRequest request) {
        Team team = teamService.findById(teamId);
        return processTeamTraining(team, request.getFocus(), request.getIntensity());
    }

    public PlayerTrainingFocus assignIndividualFocus(Long playerId, IndividualFocusRequest request) {
        Player player = playerService.findOne(playerId);

        if (playerTrainingFocusRepository.existsOverlappingFocus(player, request.getStartDate(), request.getEndDate())) {
            throw new IllegalArgumentException("Player already has an active focus during this period");
        }

        PlayerTrainingFocus focus = PlayerTrainingFocus.builder()
            .player(player)
            .focus(request.getFocus())
            .intensity(request.getIntensity())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

        return playerTrainingFocusRepository.save(focus);
    }

    public void removeIndividualFocus(Long playerId) {
        Player player = playerService.findOne(playerId);
        LocalDate today = LocalDate.now();
        List<PlayerTrainingFocus> activeFocuses = playerTrainingFocusRepository.findActiveFocus(player, today);

        for (PlayerTrainingFocus focus : activeFocuses) {
            if (focus.getStartDate().isEqual(today)) {
                playerTrainingFocusRepository.delete(focus);
            } else {
                focus.setEndDate(today.minusDays(1)); // End yesterday
                playerTrainingFocusRepository.save(focus);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<IndividualFocusDTO> getTeamIndividualFocuses(Long teamId) {
        Team team = teamService.findById(teamId);
        List<Player> players = team.getPlayers();
        LocalDate today = LocalDate.now();

        List<PlayerTrainingFocus> activeFocuses = playerTrainingFocusRepository.findActiveFocusForPlayers(players, today);

        return activeFocuses.stream()
            .map(f -> IndividualFocusDTO.builder()
                .id(f.getId())
                .playerId(f.getPlayer().getId())
                .playerName(f.getPlayer().getName() + " " + f.getPlayer().getSurname())
                .focus(f.getFocus())
                .intensity(f.getIntensity())
                .startDate(f.getStartDate())
                .endDate(f.getEndDate())
                .build())
            .toList();
    }

}
