package com.lollito.fm.model.dto;

import java.time.LocalDateTime;

import com.lollito.fm.model.ActivitySeverity;
import com.lollito.fm.model.ActivityType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserActivityDTO {
    private Long id;
    private ActivityType activityType;
    private String activityDescription;
    private LocalDateTime activityTimestamp;
    private ActivitySeverity severity;
    private String ipAddress;
}
