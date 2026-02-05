package com.lollito.fm.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebugDashboardDTO {
    private List<DebugActionDTO> recentDebugActions;
    private List<SystemSnapshotDTO> systemSnapshots;
    private List<PerformanceMetricDTO> performanceMetrics;
    private List<TestScenarioDTO> activeTestScenarios;
    private SystemHealthMetricsDTO systemHealth;
    private Long debugActionsLast24Hours;
}
