package com.lollito.fm.model.rest;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

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
