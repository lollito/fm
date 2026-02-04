package com.lollito.fm.model.dto;

import lombok.Data;

@Data
public class CreateStadiumRequest {
    private String name;
    private Integer capacity;
    private Integer pitchQuality;
    private Integer facilitiesQuality;
}
