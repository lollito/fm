package com.lollito.fm.model.dto;

import java.time.LocalDateTime;
import com.lollito.fm.model.DebugActionStatus;
import com.lollito.fm.model.DebugActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebugActionDTO {
    private Long id;
    private String adminUsername;
    private DebugActionType actionType;
    private String actionName;
    private String actionDescription;
    private String targetEntityType;
    private Long targetEntityId;
    private String targetEntityName;
    private LocalDateTime executedAt;
    private DebugActionStatus status;
    private String result;
    private String impactSummary;
}
