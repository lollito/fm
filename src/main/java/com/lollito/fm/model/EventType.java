package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum EventType {
    GOAL("Goal", "⚽", EventSeverity.MAJOR),
    ASSIST("Assist", "🅰️", EventSeverity.NORMAL),
    YELLOW_CARD("Yellow Card", "🟨", EventSeverity.NORMAL),
    RED_CARD("Red Card", "🟥", EventSeverity.MAJOR),
    SUBSTITUTION("Substitution", "🔄", EventSeverity.NORMAL),
    INJURY("Injury", "🏥", EventSeverity.NORMAL),
    OFFSIDE("Offside", "🚩", EventSeverity.MINOR),
    FOUL("Foul", "⚠️", EventSeverity.MINOR),
    CORNER("Corner", "📐", EventSeverity.MINOR),
    FREE_KICK("Free Kick", "🦶", EventSeverity.MINOR),
    PENALTY("Penalty", "⚽", EventSeverity.MAJOR),
    SAVE("Save", "🥅", EventSeverity.NORMAL),
    SHOT_ON_TARGET("Shot on Target", "🎯", EventSeverity.MINOR),
    SHOT_OFF_TARGET("Shot off Target", "❌", EventSeverity.MINOR),
    POSSESSION_CHANGE("Possession Change", "🔄", EventSeverity.MINOR),
    TACTICAL_CHANGE("Tactical Change", "📋", EventSeverity.NORMAL),
    HALF_TIME("Half Time", "⏸️", EventSeverity.NORMAL),
    FULL_TIME("Full Time", "⏹️", EventSeverity.MAJOR),
    KICK_OFF("Kick Off", "⚽", EventSeverity.NORMAL);

    private final String displayName;
    private final String icon;
    private final EventSeverity defaultSeverity;

    EventType(String displayName, String icon, EventSeverity defaultSeverity) {
        this.displayName = displayName;
        this.icon = icon;
        this.defaultSeverity = defaultSeverity;
    }
}
