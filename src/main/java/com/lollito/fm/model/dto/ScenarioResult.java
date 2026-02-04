package com.lollito.fm.model.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResult {
    private boolean success;
    private String failureReason;
    private Map<String, Object> outcomes;
}
