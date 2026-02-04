package com.lollito.fm.model.dto;

import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;

import lombok.Data;

@Data
public class TrainingPlanRequest {
    private TrainingFocus mondayFocus;
    private TrainingFocus tuesdayFocus;
    private TrainingFocus wednesdayFocus;
    private TrainingFocus thursdayFocus;
    private TrainingFocus fridayFocus;
    private TrainingFocus saturdayFocus;
    private TrainingFocus sundayFocus;
    private TrainingIntensity intensity;
    private Boolean restOnWeekends;
}
