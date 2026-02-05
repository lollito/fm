package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum StaffRole {
    HEAD_COACH("Head Coach", 50000, 200000),
    ASSISTANT_COACH("Assistant Coach", 20000, 80000),
    FITNESS_COACH("Fitness Coach", 15000, 60000),
    GOALKEEPING_COACH("Goalkeeping Coach", 18000, 70000),
    YOUTH_COACH("Youth Coach", 12000, 50000),
    HEAD_PHYSIO("Head Physio", 25000, 90000),
    PHYSIO("Physio", 15000, 60000),
    DOCTOR("Doctor", 30000, 120000),
    HEAD_SCOUT("Head Scout", 20000, 80000),
    SCOUT("Scout", 10000, 40000),
    ANALYST("Analyst", 15000, 55000);

    private final String displayName;
    private final int minSalary;
    private final int maxSalary;

    StaffRole(String displayName, int minSalary, int maxSalary) {
        this.displayName = displayName;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
    }
}
