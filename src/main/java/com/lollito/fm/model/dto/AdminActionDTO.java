package com.lollito.fm.model.dto;

import java.time.LocalDateTime;

import com.lollito.fm.model.ActionStatus;
import com.lollito.fm.model.AdminActionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminActionDTO {
    private Long id;
    private String adminUsername;
    private AdminActionType actionType;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String actionDescription;
    private LocalDateTime actionTimestamp;
    private ActionStatus status;
}
