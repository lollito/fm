package com.lollito.fm.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulateMatchesRequest {
    private List<Long> matchIds;
    private String forceResult; // HOME_WIN, AWAY_WIN, DRAW
}
