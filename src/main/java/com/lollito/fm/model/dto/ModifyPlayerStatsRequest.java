package com.lollito.fm.model.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyPlayerStatsRequest {
    private List<Long> playerIds;
    private Map<String, Integer> statModifications;
    private Integer overallAdjustment;
    private Integer ageAdjustment;
}
