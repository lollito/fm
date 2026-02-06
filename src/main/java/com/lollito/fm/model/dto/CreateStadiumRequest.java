package com.lollito.fm.model.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;

@Data
public class CreateStadiumRequest {
    private String name;

    @Min(value = 0, message = "Capacity must be positive")
    private Integer capacity;

    @Min(value = 0, message = "Pitch quality must be positive")
    private Integer pitchQuality;

    @Min(value = 0, message = "Facilities quality must be positive")
    private Integer facilitiesQuality;
}
