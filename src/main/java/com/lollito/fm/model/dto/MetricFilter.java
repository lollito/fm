package com.lollito.fm.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.lollito.fm.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricFilter {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<MetricType> metricTypes;
}
