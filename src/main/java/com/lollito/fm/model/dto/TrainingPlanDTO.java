package com.lollito.fm.model.dto;

import java.time.LocalDateTime;

import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;

import lombok.Data;

@Data
public class TrainingPlanDTO {
    private Long id;
    private Long teamId;
    private TrainingFocus mondayFocus;
    private TrainingFocus tuesdayFocus;
    private TrainingFocus wednesdayFocus;
    private TrainingFocus thursdayFocus;
    private TrainingFocus fridayFocus;
    private TrainingFocus saturdayFocus;
    private TrainingFocus sundayFocus;
    private TrainingIntensity intensity;
    private Boolean restOnWeekends;
    private LocalDateTime lastUpdated;
}
