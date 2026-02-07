package com.lollito.fm.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IndividualTrainingFocus {
    STAMINA("stamina"),
    PLAYMAKING("playmaking"),
    SCORING("scoring"),
    WINGER("winger"),
    GOALKEEPING("goalkeeping"),
    PASSING("passing"),
    DEFENDING("defending"),
    SET_PIECES("setPieces");

    private final String fieldName;
}
