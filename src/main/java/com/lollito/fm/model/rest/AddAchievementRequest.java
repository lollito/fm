package com.lollito.fm.model.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.lollito.fm.model.AchievementType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddAchievementRequest {
    private AchievementType type;
    private String title;
    private String description;
}
