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
@Table(name = "debug_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class DebugAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    @ToString.Exclude
    private User adminUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private DebugActionType actionType;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "action_description")
    private String actionDescription;

    // Target entities
    @Column(name = "target_entity_type")
    private String targetEntityType; // Season, Match, League, etc.

    @Column(name = "target_entity_id")
    private Long targetEntityId;

    @Column(name = "target_entity_name")
    private String targetEntityName;

    // Action parameters (JSON)
    @Column(name = "action_parameters", length = 4000)
    private String actionParameters;

    // Execution details
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Enumerated(EnumType.STRING)
    private DebugActionStatus status;

    @Column(name = "result", length = 4000)
    private String result; // JSON result data

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @Column(name = "stack_trace", length = 4000)
    private String stackTrace;

    // Impact tracking
    @Column(name = "entities_affected")
    private Integer entitiesAffected;

    @Column(name = "impact_summary")
    private String impactSummary;

    @Column(name = "is_reversible")
    private Boolean isReversible;

    @Column(name = "reversal_instructions")
    private String reversalInstructions;
}
