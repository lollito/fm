package com.lollito.fm.model.dto;

import java.time.LocalDateTime;
import com.lollito.fm.model.MetricSeverity;
import com.lollito.fm.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetricDTO {
    private Long id;
    private MetricType metricType;
    private String metricName;
    private Double metricValue;
    private String metricUnit;
    private LocalDateTime recordedAt;
    private MetricSeverity severity;
    private String metricMetadata;
}
