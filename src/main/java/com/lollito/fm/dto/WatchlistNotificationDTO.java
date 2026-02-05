package com.lollito.fm.dto;

import java.time.LocalDateTime;

import com.lollito.fm.model.NotificationSeverity;
import com.lollito.fm.model.WatchlistNotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchlistNotificationDTO {
    private Long id;
    private WatchlistNotificationType type;
    private String title;
    private String message;
    private String detailedMessage;
    private LocalDateTime createdDate;
    private Boolean isRead;
    private Boolean isImportant;
    private NotificationSeverity severity;
}
