package com.lollito.fm.model.dto;

import java.util.Set;

import com.lollito.fm.model.SnapshotScope;
import com.lollito.fm.model.SnapshotType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSnapshotRequest {
    private String name;
    private String description;
    private SnapshotType type;
    private Set<SnapshotScope> scopes;
}
