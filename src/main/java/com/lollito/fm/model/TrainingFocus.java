package com.lollito.fm.model;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrainingFocus {
    ATTACKING("Attacking", List.of("scoring", "winger", "passing")),
    DEFENDING("Defending", List.of("defending", "playmaking")),
    PHYSICAL("Physical", List.of("stamina")),
    TECHNICAL("Technical", List.of("passing", "setPieces")),
    GOALKEEPING("Goalkeeping", List.of("goalkeeping", "setPieces")),
    BALANCED("Balanced", List.of("playmaking", "passing", "defending"));

    private final String displayName;
    private final List<String> affectedSkills;
}
