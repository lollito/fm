package com.lollito.fm.model.rest;

import com.lollito.fm.model.PlayerAchievementType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddAchievementRequest {
    private PlayerAchievementType type;
    private String title;
    private String description;
}
