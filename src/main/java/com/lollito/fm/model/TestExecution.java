package com.lollito.fm.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "test_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class TestExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_scenario_id")
    @ToString.Exclude
    private TestScenario testScenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_user_id")
    @ToString.Exclude
    private User executedBy;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Enumerated(EnumType.STRING)
    private TestExecutionStatus status;

    // Execution results (JSON)
    @Column(name = "execution_results", length = 4000)
    private String executionResults;

    @Column(name = "actual_outcomes", length = 4000)
    private String actualOutcomes;

    private Boolean passed;

    @Column(name = "failure_reason")
    private String failureReason;

    // Performance metrics
    @Column(name = "queries_executed")
    private Integer queriesExecuted;

    @Column(name = "memory_used_mb")
    private Long memoryUsedMb;

    @Column(name = "cpu_usage_percent")
    private Double cpuUsagePercent;
}
