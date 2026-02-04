package com.lollito.fm.model.dto;

import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;

import lombok.Data;

@Data
public class ManualTrainingRequest {
    private TrainingFocus focus;
    private TrainingIntensity intensity;
}
