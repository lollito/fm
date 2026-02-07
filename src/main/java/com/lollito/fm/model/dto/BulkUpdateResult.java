package com.lollito.fm.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
// Assuming PlayerDTO exists or we just return generic objects.
// I'll check if PlayerDTO exists. The snippet says `updatedEntities(updatedPlayers.stream().map(this::convertToDTO).collect(Collectors.toList()))`
// I'll use Object or check existing DTOs. `src/main/java/com/lollito/fm/model/dto` did not show PlayerDTO.
// But `src/main/java/com/lollito/fm/model/rest/` might have something.
// `PlayerController` likely uses `Player` entity directly or a DTO.
// `PlayerService` returns `Player`.
// I'll use `Object` for now to avoid dependency hell if PlayerDTO doesn't exist.

@Data
@Builder
public class BulkUpdateResult {
    private int totalRequested;
    private int successfulUpdates;
    private int failedUpdates;
    private List<Object> updatedEntities;
    private List<String> errors;
}
