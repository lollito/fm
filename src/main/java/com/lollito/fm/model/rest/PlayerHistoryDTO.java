package com.lollito.fm.model.rest;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerHistoryDTO {
    private PlayerDTO player;
    private PlayerCareerStatsDTO careerStats;
    private List<PlayerSeasonStatsDTO> seasonStats;
    private List<PlayerAchievementDTO> achievements;
    private List<PlayerTransferHistoryDTO> transferHistory;
}
