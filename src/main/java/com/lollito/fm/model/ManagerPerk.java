package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum ManagerPerk {

    // Tactical
    VIDEO_ANALYST("Video Analyst", "Increases tactical learning speed by 5%.", 1, Category.TACTICAL),
    MOTIVATOR("Motivator", "Increases morale recovery after defeat by 10%.", 5, Category.TACTICAL),
    FORTRESS("Fortress", "Increases home advantage bonus by 2%.", 10, Category.TACTICAL),

    // Financial
    NEGOTIATOR("Negotiator", "Reduces wage demands by 5%.", 1, Category.FINANCIAL),
    MARKETING_GURU("Marketing Guru", "Increases sponsor revenue by 5%.", 5, Category.FINANCIAL),
    INVESTOR("Investor", "Earns interest on bank deposits.", 10, Category.FINANCIAL),

    // Scouting
    HAWK_EYE("Hawk Eye", "Reduces scouting time by 10%.", 1, Category.SCOUTING),
    GLOBAL_NETWORK("Global Network", "Increases probability of finding Wonderkids.", 5, Category.SCOUTING),
    PERSUADER("Persuader", "Increases probability of negotiation acceptance.", 10, Category.SCOUTING);

    private final String name;
    private final String description;
    private final Integer requiredLevel;
    private final Category category;

    ManagerPerk(String name, String description, Integer requiredLevel, Category category) {
        this.name = name;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.category = category;
    }

    public enum Category {
        TACTICAL,
        FINANCIAL,
        SCOUTING
    }
}
