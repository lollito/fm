package com.lollito.fm.dto;

import lombok.Data;

@Data
public class AssignPlayerScoutingRequest {
    private Long scoutId;
    private Long playerId;
    private Integer priority;
    private String instructions;
}
