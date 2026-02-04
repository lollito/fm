package com.lollito.fm.model.dto;

import java.time.LocalDateTime;
import java.util.Set;
import com.lollito.fm.model.SnapshotScope;
import com.lollito.fm.model.SnapshotStatus;
import com.lollito.fm.model.SnapshotType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSnapshotDTO {
    private Long id;
    private String snapshotName;
    private String description;
    private SnapshotType snapshotType;
    private LocalDateTime createdAt;
    private String createdBy;
    private Set<SnapshotScope> scopes;
    private SnapshotStatus status;
    private Boolean isRestorable;
}
