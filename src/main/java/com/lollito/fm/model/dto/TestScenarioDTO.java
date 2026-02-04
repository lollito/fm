package com.lollito.fm.model.dto;

import java.time.LocalDateTime;
import com.lollito.fm.model.ScenarioCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScenarioDTO {
    private Long id;
    private String scenarioName;
    private String description;
    private ScenarioCategory category;
    private String scenarioConfig;
    private String expectedOutcomes;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
}
