package com.lollito.fm.model.dto;

import com.lollito.fm.model.EventSeverity;
import com.lollito.fm.model.EventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEventDTO {
    private Long id;
    private Long matchId;
    private EventType eventType;
    private Integer minute;
    private Integer additionalTime;
    private String description;
    private String detailedDescription;
    private String playerName;
    private String teamName;
    private Integer homeScore;
    private Integer awayScore;
    private EventSeverity severity;
    private Boolean isKeyEvent;
}
