package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum AchievementType {
    WIN_LEAGUE("League Champion", "Win the league title", 1000),
    WIN_CUP("Cup Winner", "Win the cup title", 500),
    PROMOTION("Promotion", "Gain promotion to a higher league", 800),
    TYCOON("Tycoon", "Accumulate a balance of 100M", 300),
    SCROOGE("Scrooge", "Complete a season with zero spending", 200),
    GOLEADOR("Goleador", "Win a match by 5 goals or more", 150),
    COMEBACK_KING("Comeback King", "Win a match after being 0-2 down", 250),
    ACADEMY_HERO("Academy Hero", "Have a youth academy graduate score a goal", 100),
    TRADER("Trader", "Make a profit on a player sale", 100);

    private final String name;
    private final String description;
    private final int xpReward;

    AchievementType(String name, String description, int xpReward) {
        this.name = name;
        this.description = description;
        this.xpReward = xpReward;
    }
}
