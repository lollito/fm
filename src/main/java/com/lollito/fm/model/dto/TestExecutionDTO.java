package com.lollito.fm.model.dto;

import java.time.LocalDateTime;

import com.lollito.fm.model.TestExecutionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionDTO {
    private Long id;
    private Long scenarioId;
    private String scenarioName;
    private String executedBy;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long executionTimeMs;
    private TestExecutionStatus status;
    private Boolean passed;
    private String failureReason;
    private String executionResults; // JSON string or Object
    private String actualOutcomes; // JSON string or Object
}
