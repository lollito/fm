package com.lollito.fm.model.dto;

import java.time.LocalDate;

import com.lollito.fm.model.IndividualTrainingFocus;
import com.lollito.fm.model.TrainingIntensity;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class IndividualFocusDTO {
    private Long id;
    private Long playerId;
    private String playerName;
    private IndividualTrainingFocus focus;
    private TrainingIntensity intensity;
    private LocalDate startDate;
    private LocalDate endDate;
}
