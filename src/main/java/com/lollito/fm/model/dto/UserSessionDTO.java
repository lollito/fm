package com.lollito.fm.model.dto;

import java.time.LocalDateTime;

import com.lollito.fm.model.SessionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSessionDTO {
    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private String location;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;
    private SessionStatus status;
    private Boolean isActive;
}
