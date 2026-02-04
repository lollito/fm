package com.lollito.fm.model.dto;

import lombok.Data;

@Data
public class PlayerGenerationRequest {
    private Integer targetSquadSize;
    private QualityRange qualityRange;
}
