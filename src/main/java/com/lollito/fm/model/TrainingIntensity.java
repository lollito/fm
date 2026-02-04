package com.lollito.fm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrainingIntensity {
    LIGHT(0.5, 0.1),     // Low improvement, low fatigue
    MODERATE(1.0, 0.3),  // Normal improvement, normal fatigue
    INTENSIVE(1.5, 0.6), // High improvement, high fatigue
    RECOVERY(0.2, -0.5); // Minimal improvement, condition recovery

    private final double improvementMultiplier;
    private final double fatigueMultiplier;
}
