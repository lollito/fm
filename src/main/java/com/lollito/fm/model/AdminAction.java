package com.lollito.fm.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "admin_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class AdminAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    @ToString.Exclude
    @JsonIgnore
    private User adminUser;

    @Enumerated(EnumType.STRING)
    private AdminActionType actionType;

    private String entityType; // Club, Player, League, etc.
    private Long entityId;
    private String entityName;

    private String actionDescription;
    private String oldValues; // JSON of previous values
    private String newValues; // JSON of new values

    private LocalDateTime actionTimestamp;
    private String ipAddress;
    private String userAgent;

    @Enumerated(EnumType.STRING)
    private ActionStatus status; // SUCCESS, FAILED, PENDING

    private String failureReason;
    private String notes;
}
