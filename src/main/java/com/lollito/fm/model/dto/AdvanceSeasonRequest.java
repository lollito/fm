package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceSeasonRequest {
    private Boolean skipRemainingMatches;
    private Boolean generateNewPlayers;
    private Boolean processTransfers;
}
