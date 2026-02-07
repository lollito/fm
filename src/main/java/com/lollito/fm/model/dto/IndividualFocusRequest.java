package com.lollito.fm.model.dto;

import java.time.LocalDate;

import com.lollito.fm.model.IndividualTrainingFocus;
import com.lollito.fm.model.TrainingIntensity;

import lombok.Data;

@Data
public class IndividualFocusRequest {
    private IndividualTrainingFocus focus;
    private TrainingIntensity intensity;
    private LocalDate startDate;
    private LocalDate endDate;
}
