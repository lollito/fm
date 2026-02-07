package com.lollito.fm.model.rest;

import java.time.LocalDate;

import com.lollito.fm.model.AchievementType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Season;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAchievementDTO {
    private Long id;
    private AchievementType type;
    private String title;
    private String description;
    private LocalDate dateAchieved;
    private Club club;
    private Season season;
}
