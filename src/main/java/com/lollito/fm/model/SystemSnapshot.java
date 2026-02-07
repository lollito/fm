package com.lollito.fm.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "system_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SystemSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "snapshot_name")
    private String snapshotName;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_type")
    private SnapshotType snapshotType; // MANUAL, AUTOMATIC, PRE_DEBUG

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    // Snapshot data (JSON)
    @Column(name = "game_state_data", length = 4000)
    private String gameStateData;

    @Column(name = "database_metrics", length = 4000)
    private String databaseMetrics;

    @Column(name = "system_metrics", length = 4000)
    private String systemMetrics;

    // Snapshot scope
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<SnapshotScope> scopes = new HashSet<>();

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "checksum_md5")
    private String checksumMd5;

    @Enumerated(EnumType.STRING)
    private SnapshotStatus status; // CREATING, READY, CORRUPTED, DELETED

    @Column(name = "is_restorable")
    private Boolean isRestorable;

    @Column(name = "storage_location")
    private String storageLocation;
}
