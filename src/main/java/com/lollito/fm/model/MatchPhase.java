package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum MatchPhase {
    PRE_MATCH("Pre-Match", 0),
    FIRST_HALF("First Half", 45),
    HALF_TIME("Half Time", 0),
    SECOND_HALF("Second Half", 45),
    EXTRA_TIME_FIRST("Extra Time 1st", 15),
    EXTRA_TIME_SECOND("Extra Time 2nd", 15),
    PENALTIES("Penalties", 0),
    FINISHED("Finished", 0);

    private final String displayName;
    private final int duration;

    MatchPhase(String displayName, int duration) {
        this.displayName = displayName;
        this.duration = duration;
    }
}
