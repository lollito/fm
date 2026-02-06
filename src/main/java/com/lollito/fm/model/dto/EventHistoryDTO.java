package com.lollito.fm.model.dto;

import com.lollito.fm.model.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHistoryDTO {
    private Long id;
    private String event;
    private Integer minute;
    private Event type;
    private Integer homeScore;
    private Integer awayScore;
    private String eventType;
    private String description;
    private Boolean isKeyEvent;
}
