package com.lollito.fm.model.rest;

import com.lollito.fm.model.AchievementType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddAchievementRequest {
    private AchievementType type;
    private String title;
    private String description;
}
