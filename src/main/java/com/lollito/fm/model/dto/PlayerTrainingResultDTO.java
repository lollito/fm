package com.lollito.fm.model.dto;

import com.lollito.fm.model.TrainingPerformance;

import lombok.Data;

@Data
public class PlayerTrainingResultDTO {
    private Long id;
    private PlayerSummaryDTO player;
    private Long trainingSessionId;
    private Double attendanceRate;
    private Double improvementGained;
    private Double fatigueGained;
    private TrainingPerformance performance;

    @Data
    public static class PlayerSummaryDTO {
        private Long id;
        private String name;
        private String surname;
    }
}
