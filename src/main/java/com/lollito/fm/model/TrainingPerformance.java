package com.lollito.fm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrainingPerformance {
    POOR(0.5), AVERAGE(1.0), GOOD(1.3), EXCELLENT(1.6);

    private final double multiplier;
}
