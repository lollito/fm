package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthMetricsDTO {
    private Double memoryUsagePercent;
    private Long totalMemoryMb;
    private Long usedMemoryMb;
    private Long freeMemoryMb;
    private Integer activeThreads;
    private Integer databaseConnectionsActive;
    private Double systemUptime;
}
