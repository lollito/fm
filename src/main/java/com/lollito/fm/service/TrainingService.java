package com.lollito.fm.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerTrainingResult;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;
import com.lollito.fm.model.TrainingPerformance;
import com.lollito.fm.model.TrainingPlan;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.TrainingStatus;
import com.lollito.fm.model.dto.ManualTrainingRequest;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.dto.TrainingPlanRequest;
import com.lollito.fm.repository.PlayerTrainingResultRepository;
import com.lollito.fm.repository.TrainingPlanRepository;
import com.lollito.fm.repository.TrainingSessionRepository;
import com.lollito.fm.repository.rest.ClubRepository;
import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private PlayerTrainingResultRepository playerTrainingResultRepository;

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

    /**
     * Process daily training for all teams
     */
    @Scheduled(cron = "${fm.training.schedule.cron:0 0 10 * * MON-FRI}")
    public void processDailyTraining() {
        logger.info("Starting daily training processing");
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
                logger.error("Error processing training for team {}", plan.getTeam().getId(), e);
            }
        }
        logger.info("Completed daily training processing");
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
        TrainingSession session = TrainingSession.builder()
            .team(team)
            .focus(focus)
            .intensity(intensity)
            .startDate(LocalDate.now())
            .status(TrainingStatus.ACTIVE)
            .effectivenessMultiplier(calculateEffectiveness(team))
            .build();

        session = trainingSessionRepository.save(session);

        // Process each player's training
        List<Player> players = team.getPlayers();
        for (Player player : players) {
            if (!player.isInjured() && player.getCondition() > 20) {
                PlayerTrainingResult result = processPlayerTraining(player, session);
                session.getPlayerResults().add(result);
            }
        }

        session.setStatus(TrainingStatus.COMPLETED);
        session.setEndDate(LocalDate.now());

        return trainingSessionRepository.save(session);
    }

    /**
     * Calculate training effectiveness based on facilities and staff
     */
    public Double calculateEffectiveness(Team team) {
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
            if (bonuses != null && bonuses.getTrainingBonus() != null) {
                baseEffectiveness += bonuses.getTrainingBonus();
            }
        }

        return Math.min(2.0, baseEffectiveness); // Cap at 2x effectiveness
    }

    /**
     * Process individual player training
     */
    private PlayerTrainingResult processPlayerTraining(Player player,
                                                     TrainingSession session) {
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

        // Apply improvement to relevant skills
        applySkillImprovement(player, session.getFocus(), improvement);

        // Calculate and apply fatigue
        double fatigue = session.getIntensity().getFatigueMultiplier() *
                        attendanceRate * 10; // 10 condition points base

        // Handle negative fatigue (recovery)
        if (fatigue > 0) {
             player.decrementCondition(fatigue);
        } else {
             // If fatigue is negative, it means recovery
             player.incrementCondition(-fatigue);
        }

        // Save player changes
        playerService.save(player);

        // Create training result record
        PlayerTrainingResult result = PlayerTrainingResult.builder()
            .player(player)
            .trainingSession(session)
            .attendanceRate(attendanceRate)
            .improvementGained(improvement)
            .fatigueGained(fatigue)
            .performance(performance)
            .build();

        return playerTrainingResultRepository.save(result);
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

    /**
     * Apply skill improvements based on training focus
     */
    private void applySkillImprovement(Player player, TrainingFocus focus,
                                     double improvement) {
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

    public Page<TrainingSession> getTrainingHistory(Long teamId, Pageable pageable) {
        Team team = teamService.findById(teamId);
        return trainingSessionRepository.findByTeam(team, pageable);
    }

    public List<PlayerTrainingResult> getSessionResults(Long sessionId) {
        return trainingSessionRepository.findById(sessionId)
            .map(TrainingSession::getPlayerResults)
            .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public TrainingSession createManualTrainingSession(Long teamId, ManualTrainingRequest request) {
        Team team = teamService.findById(teamId);
        return processTeamTraining(team, request.getFocus(), request.getIntensity());
    }

}
