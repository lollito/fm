package com.lollito.fm.model.dto;

import java.time.LocalDate;
import java.util.List;

import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;
import com.lollito.fm.model.TrainingStatus;

import lombok.Data;

@Data
public class TrainingSessionDTO {
    private Long id;
    private Long teamId;
    private TrainingFocus focus;
    private TrainingIntensity intensity;
    private LocalDate startDate;
    private LocalDate endDate;
    private TrainingStatus status;
    private Double effectivenessMultiplier;
    private Integer playerCount;
    private List<PlayerTrainingResultDTO> playerResults;
}
