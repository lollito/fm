package com.lollito.fm.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerGenerationResult {
    private int totalGenerated;
    private int playersPerClub;
    private List<Object> generatedPlayers;
}
