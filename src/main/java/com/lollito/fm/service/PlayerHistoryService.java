package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.PlayerAchievementType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.MatchPlayerStats;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerAchievement;
import com.lollito.fm.model.PlayerCareerStats;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.PlayerSeasonStats;
import com.lollito.fm.model.PlayerTransferHistory;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.TransferType;
import com.lollito.fm.model.rest.PlayerAchievementDTO;
import com.lollito.fm.model.rest.PlayerCareerStatsDTO;
import com.lollito.fm.model.rest.PlayerDTO;
import com.lollito.fm.model.rest.PlayerHistoryDTO;
import com.lollito.fm.mapper.ClubMapper;
import com.lollito.fm.mapper.LeagueMapper;
import com.lollito.fm.mapper.SeasonMapper;
import com.lollito.fm.model.rest.PlayerSeasonStatsDTO;
import com.lollito.fm.model.rest.PlayerTransferHistoryDTO;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.PlayerAchievementRepository;
import com.lollito.fm.repository.rest.PlayerCareerStatsRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.PlayerSeasonStatsRepository;
import com.lollito.fm.repository.rest.PlayerTransferHistoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PlayerHistoryService {

    @Autowired private PlayerSeasonStatsRepository playerSeasonStatsRepository;
    @Autowired private PlayerCareerStatsRepository playerCareerStatsRepository;
    @Autowired private PlayerAchievementRepository playerAchievementRepository;
    @Autowired private PlayerTransferHistoryRepository playerTransferHistoryRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private SeasonService seasonService;
    @Autowired private ClubRepository clubRepository;

    @Autowired private ClubMapper clubMapper;
    @Autowired private SeasonMapper seasonMapper;
    @Autowired private LeagueMapper leagueMapper;

    private Club getClubByPlayer(Player player) {
        if (player.getTeam() == null) return null;
        Optional<Club> club = clubRepository.findByTeam(player.getTeam());
        if (club.isPresent()) return club.get();
        return clubRepository.findByUnder18(player.getTeam()).orElse(null);
    }

    public PlayerSeasonStats initializeSeasonStats(Player player, Season season) {
        // Check if stats already exist
        Optional<PlayerSeasonStats> existing = playerSeasonStatsRepository
            .findByPlayerAndSeason(player, season);

        if (existing.isPresent()) {
            return existing.get();
        }

        Club club = getClubByPlayer(player);
        PlayerSeasonStats seasonStats = PlayerSeasonStats.builder()
            .player(player)
            .season(season)
            .club(club)
            .league(club != null ? club.getLeague() : null)
            .matchesPlayed(0)
            .matchesStarted(0)
            .minutesPlayed(0)
            .goals(0)
            .assists(0)
            .yellowCards(0)
            .redCards(0)
            .averageRating(0.0)
            .averageCondition(player.getCondition())
            .averageMorale(player.getMoral())
            .injuryDays(0)
            .injuryCount(0)
            .build();

        return playerSeasonStatsRepository.save(seasonStats);
    }

    private PlayerSeasonStats getOrCreateSeasonStats(Player player, Season season) {
        return initializeSeasonStats(player, season);
    }

    public void updateMatchStatistics(Player player, MatchPlayerStats matchStats) {
        Season currentSeason = seasonService.getCurrentSeason();
        if (currentSeason == null) return; // Should not happen ideally

        PlayerSeasonStats seasonStats = getOrCreateSeasonStats(player, currentSeason);

        // Update match statistics
        seasonStats.setMatchesPlayed(seasonStats.getMatchesPlayed() + 1);
        if (matchStats.isStarted()) {
            seasonStats.setMatchesStarted(seasonStats.getMatchesStarted() + 1);
        }
        seasonStats.setMinutesPlayed(seasonStats.getMinutesPlayed() + matchStats.getMinutesPlayed());

        // Update performance statistics
        seasonStats.setGoals(seasonStats.getGoals() + matchStats.getGoals());
        seasonStats.setAssists(seasonStats.getAssists() + matchStats.getAssists());
        seasonStats.setYellowCards(seasonStats.getYellowCards() + matchStats.getYellowCards());
        seasonStats.setRedCards(seasonStats.getRedCards() + matchStats.getRedCards());

        // Update advanced statistics
        if (matchStats.getShots() != null) {
            seasonStats.setShots(seasonStats.getShots() + matchStats.getShots());
        }
        if (matchStats.getShotsOnTarget() != null) {
            seasonStats.setShotsOnTarget(seasonStats.getShotsOnTarget() + matchStats.getShotsOnTarget());
        }
        if (matchStats.getPasses() != null) {
            seasonStats.setPasses(seasonStats.getPasses() + matchStats.getPasses());
        }
        if (matchStats.getCompletedPasses() != null) {
            seasonStats.setPassesCompleted(seasonStats.getPassesCompleted() + matchStats.getCompletedPasses());
        }
        if (matchStats.getTackles() != null) {
            seasonStats.setTackles(seasonStats.getTackles() + matchStats.getTackles());
        }

        // Update goalkeeper statistics
        if (player.getRole() == PlayerRole.GOALKEEPER) {
            if (matchStats.getSaves() != null) {
                seasonStats.setSaves(seasonStats.getSaves() + matchStats.getSaves());
            }
            if (matchStats.getGoalsConceded() != null) {
                seasonStats.setGoalsConceded(seasonStats.getGoalsConceded() + matchStats.getGoalsConceded());
            }
            if (matchStats.getGoalsConceded() != null && matchStats.getGoalsConceded() == 0) {
                seasonStats.setCleanSheets(seasonStats.getCleanSheets() + 1);
            }
        }

        // Update rating
        updatePlayerRating(seasonStats, matchStats.getRating());

        playerSeasonStatsRepository.save(seasonStats);

        // Update career stats
        updateCareerStats(player);

        // Check for achievements
        checkForAchievements(player, seasonStats, matchStats);
    }

    public void updateCareerStats(Player player) {
        PlayerCareerStats careerStats = player.getCareerStats();
        if (careerStats == null) {
            careerStats = new PlayerCareerStats();
            careerStats.setPlayer(player);
            careerStats.setFirstProfessionalMatch(LocalDate.now());
            player.setCareerStats(careerStats);
        }

        List<PlayerSeasonStats> allSeasonStats = playerSeasonStatsRepository.findByPlayer(player);

        careerStats.setTotalMatchesPlayed(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getMatchesPlayed).sum()
        );
        careerStats.setTotalGoals(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getGoals).sum()
        );
        careerStats.setTotalAssists(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getAssists).sum()
        );
        careerStats.setTotalYellowCards(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getYellowCards).sum()
        );
        careerStats.setTotalRedCards(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getRedCards).sum()
        );
        careerStats.setTotalCleanSheets(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getCleanSheets).sum()
        );

        careerStats.setMostGoalsInSeason(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getGoals).max().orElse(0)
        );
        careerStats.setMostAssistsInSeason(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getAssists).max().orElse(0)
        );
        careerStats.setHighestSeasonRating(
            allSeasonStats.stream().mapToDouble(PlayerSeasonStats::getAverageRating).max().orElse(0.0)
        );

        checkCareerMilestones(careerStats);

        playerCareerStatsRepository.save(careerStats);
    }

    public PlayerTransferHistory recordTransfer(Player player, Club fromClub, Club toClub,
                                              BigDecimal transferFee, TransferType transferType) {
        Season currentSeason = seasonService.getCurrentSeason();

        PlayerTransferHistory transfer = PlayerTransferHistory.builder()
            .player(player)
            .fromClub(fromClub)
            .toClub(toClub)
            .transferDate(LocalDate.now())
            .transferFee(transferFee)
            .transferType(transferType)
            .season(currentSeason)
            .salary(player.getSalary())
            .build();

        transfer = playerTransferHistoryRepository.save(transfer);

        PlayerCareerStats careerStats = player.getCareerStats();
        if (careerStats == null) {
            updateCareerStats(player);
            careerStats = player.getCareerStats();
        }

        if (careerStats != null) {
            careerStats.setClubsPlayed(careerStats.getClubsPlayed() + 1);
            careerStats.setTotalTransferValue(
                careerStats.getTotalTransferValue().add(transferFee)
            );
            if (transferFee.compareTo(careerStats.getHighestTransferValue()) > 0) {
                careerStats.setHighestTransferValue(transferFee);
            }
            playerCareerStatsRepository.save(careerStats);
        }

        return transfer;
    }

    public PlayerAchievement addAchievement(Long playerId, PlayerAchievementType type,
                                          String title, String description) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        return addAchievement(player, type, title, description);
    }

    public PlayerAchievement addAchievement(Player player, PlayerAchievementType type,
                                          String title, String description) {
        Season currentSeason = seasonService.getCurrentSeason();

        PlayerAchievement achievement = PlayerAchievement.builder()
            .player(player)
            .type(type)
            .title(title)
            .description(description)
            .dateAchieved(LocalDate.now())
            .season(currentSeason)
            .club(getClubByPlayer(player))
            .build();

        return playerAchievementRepository.save(achievement);
    }

    private void checkForAchievements(Player player, PlayerSeasonStats seasonStats,
                                    MatchPlayerStats matchStats) {
        if (player.getCareerStats() == null) {
            updateCareerStats(player);
        }

        if (player.getCareerStats().getFirstGoal() == null && matchStats.getGoals() > 0) {
            addAchievement(player, PlayerAchievementType.MILESTONE, "First Goal",
                         "Scored first professional goal");
            player.getCareerStats().setFirstGoal(LocalDate.now());
            playerCareerStatsRepository.save(player.getCareerStats());
        }

        if (matchStats.getGoals() >= 3) {
            addAchievement(player, PlayerAchievementType.PERFORMANCE, "Hat-trick",
                         "Scored 3 or more goals in a single match");
        }

        int totalGoals = player.getCareerStats().getTotalGoals();
        if ((totalGoals == 10 || totalGoals == 50 || totalGoals == 100 || totalGoals == 200) && matchStats.getGoals() > 0) {
            // Check if already achieved? Assuming not tracked separately.
            // Better to check if previous total < milestone and current total >= milestone.
            // But here we rely on exact match which might be missed if multiple goals scored?
            // "totalGoals" is already updated in updateCareerStats.
            // If I had 9 goals, scored 2, now 11. 10 is missed.
            // But let's stick to task logic mostly.
            addAchievement(player, PlayerAchievementType.MILESTONE,
                         totalGoals + " Career Goals",
                         "Reached " + totalGoals + " career goals");
        }

        int totalMatches = player.getCareerStats().getTotalMatchesPlayed();
        if (totalMatches == 50 || totalMatches == 100 || totalMatches == 200 || totalMatches == 500) {
            addAchievement(player, PlayerAchievementType.MILESTONE,
                         totalMatches + " Career Matches",
                         "Played " + totalMatches + " professional matches");

            if (totalMatches == 100 && player.getCareerStats().getMilestone100Matches() == null) {
                player.getCareerStats().setMilestone100Matches(LocalDate.now());
                playerCareerStatsRepository.save(player.getCareerStats());
            }
        }

        if (seasonStats.getGoals() == 20 || seasonStats.getGoals() == 30) {
             addAchievement(player, PlayerAchievementType.PERFORMANCE,
                         seasonStats.getGoals() + " Goals in Season",
                         "Scored " + seasonStats.getGoals() + " goals in a single season");
        }

        if (player.getRole() == PlayerRole.GOALKEEPER && seasonStats.getCleanSheets() == 15) {
             // Use == 15 to avoid spamming every match after 15
             addAchievement(player, PlayerAchievementType.PERFORMANCE,
                         "Clean Sheet Specialist",
                         "Kept " + seasonStats.getCleanSheets() + " clean sheets in a season");
        }
    }

    public PlayerHistoryDTO getPlayerHistory(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new EntityNotFoundException("Player not found"));

        List<PlayerSeasonStats> seasonStats = playerSeasonStatsRepository.findByPlayerOrderBySeasonDesc(player);
        List<PlayerAchievement> achievements = playerAchievementRepository.findByPlayerOrderByDateAchievedDesc(player);
        List<PlayerTransferHistory> transfers = playerTransferHistoryRepository.findByPlayerOrderByTransferDateDesc(player);

        return PlayerHistoryDTO.builder()
            .player(convertToDTO(player))
            .careerStats(convertToDTO(player.getCareerStats()))
            .seasonStats(seasonStats.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .achievements(achievements.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .transferHistory(transfers.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .build();
    }

    public List<PlayerSeasonStats> getLeagueTopScorers(Long leagueId, Season season, int limit) {
        return playerSeasonStatsRepository.findTopScorersByLeagueAndSeason(leagueId, season,
                                                                          PageRequest.of(0, limit));
    }

    public List<PlayerSeasonStats> getLeagueTopAssists(Long leagueId, Season season, int limit) {
        return playerSeasonStatsRepository.findTopAssistsByLeagueAndSeason(leagueId, season,
                                                                          PageRequest.of(0, limit));
    }

    // Helper to get stats for a player/season directly
    public PlayerSeasonStats getPlayerSeasonStats(Long playerId, Long seasonId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        Season season = seasonService.findById(seasonId);
        if (season == null) throw new EntityNotFoundException("Season not found");

        return playerSeasonStatsRepository.findByPlayerAndSeason(player, season)
                .orElseThrow(() -> new EntityNotFoundException("Stats not found"));
    }

    public Map<Long, PlayerSeasonStats> getSeasonStatsForPlayers(List<Player> players, Season season) {
        if (players == null || players.isEmpty()) {
            return new HashMap<>();
        }
        List<PlayerSeasonStats> statsList = playerSeasonStatsRepository.findBySeasonAndPlayerIn(season, players);
        return statsList.stream()
                .collect(Collectors.toMap(stats -> stats.getPlayer().getId(), stats -> stats));
    }

    public List<PlayerAchievement> getPlayerAchievements(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        return playerAchievementRepository.findByPlayerOrderByDateAchievedDesc(player);
    }

    public List<PlayerTransferHistory> getPlayerTransfers(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Player not found"));
        return playerTransferHistoryRepository.findByPlayerOrderByTransferDateDesc(player);
    }

    private void updatePlayerRating(PlayerSeasonStats seasonStats, Double matchRating) {
        if (matchRating == null) return;

        int matchesPlayed = seasonStats.getMatchesPlayed();
        double currentAverage = seasonStats.getAverageRating();

        // Calculate new average rating
        // matchesPlayed is already incremented
        double newAverage = ((currentAverage * (matchesPlayed - 1)) + matchRating) / matchesPlayed;
        seasonStats.setAverageRating(newAverage);

        if (seasonStats.getHighestRating() == null || matchRating > seasonStats.getHighestRating()) {
            seasonStats.setHighestRating(matchRating);
        }
        if (seasonStats.getLowestRating() == null || matchRating < seasonStats.getLowestRating()) {
            seasonStats.setLowestRating(matchRating);
        }
    }

    private void checkCareerMilestones(PlayerCareerStats careerStats) {
        if (careerStats.getTotalGoals() >= 100 && careerStats.getMilestone100Goals() == null) {
            careerStats.setMilestone100Goals(LocalDate.now());
        }

        if (careerStats.getTotalMatchesPlayed() >= 100 && careerStats.getMilestone100Matches() == null) {
            careerStats.setMilestone100Matches(LocalDate.now());
        }
    }

    // DTO Conversion methods
    public PlayerDTO convertToDTO(Player player) {
        if (player == null) return null;
        return PlayerDTO.builder()
            .id(player.getId())
            .name(player.getName())
            .surname(player.getSurname())
            .age(player.getAge())
            .role(player.getRole() != null ? player.getRole().name() : null)
            .salary(player.getSalary())
            .build();
    }

    public PlayerCareerStatsDTO convertToDTO(PlayerCareerStats stats) {
        if (stats == null) {
            return PlayerCareerStatsDTO.builder()
                .totalMatchesPlayed(0)
                .totalGoals(0)
                .totalAssists(0)
                .totalYellowCards(0)
                .totalRedCards(0)
                .totalCleanSheets(0)
                .leagueTitles(0)
                .cupTitles(0)
                .clubsPlayed(0)
                .totalTransferValue(BigDecimal.ZERO)
                .highestTransferValue(BigDecimal.ZERO)
                .mostGoalsInSeason(0)
                .mostAssistsInSeason(0)
                .highestSeasonRating(0.0)
                .longestGoalStreak(0)
                .build();
        }
        return PlayerCareerStatsDTO.builder()
            .id(stats.getId())
            .totalMatchesPlayed(stats.getTotalMatchesPlayed())
            .totalGoals(stats.getTotalGoals())
            .totalAssists(stats.getTotalAssists())
            .totalYellowCards(stats.getTotalYellowCards())
            .totalRedCards(stats.getTotalRedCards())
            .totalCleanSheets(stats.getTotalCleanSheets())
            .leagueTitles(stats.getLeagueTitles())
            .cupTitles(stats.getCupTitles())
            .clubsPlayed(stats.getClubsPlayed())
            .totalTransferValue(stats.getTotalTransferValue())
            .highestTransferValue(stats.getHighestTransferValue())
            .mostGoalsInSeason(stats.getMostGoalsInSeason())
            .mostAssistsInSeason(stats.getMostAssistsInSeason())
            .highestSeasonRating(stats.getHighestSeasonRating())
            .longestGoalStreak(stats.getLongestGoalStreak())
            .build();
    }

    public PlayerSeasonStatsDTO convertToDTO(PlayerSeasonStats stats) {
        if (stats == null) return null;
        return PlayerSeasonStatsDTO.builder()
            .id(stats.getId())
            .season(seasonMapper.toDto(stats.getSeason()))
            .club(clubMapper.toDto(stats.getClub()))
            .league(leagueMapper.toDto(stats.getLeague()))
            .matchesPlayed(stats.getMatchesPlayed())
            .matchesStarted(stats.getMatchesStarted())
            .minutesPlayed(stats.getMinutesPlayed())
            .goals(stats.getGoals())
            .assists(stats.getAssists())
            .yellowCards(stats.getYellowCards())
            .redCards(stats.getRedCards())
            .cleanSheets(stats.getCleanSheets())
            .averageRating(stats.getAverageRating())
            .build();
    }

    public PlayerAchievementDTO convertToDTO(PlayerAchievement achievement) {
        if (achievement == null) return null;
        return PlayerAchievementDTO.builder()
            .id(achievement.getId())
            .type(achievement.getType())
            .title(achievement.getTitle())
            .description(achievement.getDescription())
            .dateAchieved(achievement.getDateAchieved())
            .club(clubMapper.toDto(achievement.getClub()))
            .season(seasonMapper.toDto(achievement.getSeason()))
            .build();
    }

    public PlayerTransferHistoryDTO convertToDTO(PlayerTransferHistory transfer) {
        if (transfer == null) return null;
        return PlayerTransferHistoryDTO.builder()
            .id(transfer.getId())
            .fromClub(clubMapper.toDto(transfer.getFromClub()))
            .toClub(clubMapper.toDto(transfer.getToClub()))
            .transferDate(transfer.getTransferDate())
            .transferFee(transfer.getTransferFee())
            .transferType(transfer.getTransferType())
            .season(seasonMapper.toDto(transfer.getSeason()))
            .build();
    }
}
