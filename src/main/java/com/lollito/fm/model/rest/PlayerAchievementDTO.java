package com.lollito.fm.model.rest;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.lollito.fm.model.AchievementType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Season;

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
