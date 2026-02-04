package com.lollito.fm.dto;

import java.time.LocalDate;

import com.lollito.fm.model.AssignmentStatus;
import com.lollito.fm.model.ScoutingType;

import lombok.Data;

@Data
public class ScoutingAssignmentDTO {
    private Long id;
    private ScoutDTO scout;
    private Long targetPlayerId;
    private String targetPlayerName;
    private String targetPlayerSurname;
    private ScoutingType type;
    private AssignmentStatus status;
    private LocalDate assignedDate;
    private LocalDate completionDate;
    private LocalDate expectedCompletionDate;
    private Integer priority;
    private String instructions;
}
