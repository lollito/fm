package com.lollito.fm.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemHealthDTO {
    private String databaseStatus;
    private double memoryUsage;
    private int activeConnections;
    private String lastBackupDate;
    private String systemUptime;
}
