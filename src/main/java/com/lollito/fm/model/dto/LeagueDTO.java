package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueDTO {
    private Long id;
    private String name;
    private Integer promotion;
    private Integer relegation;
    private Integer euroCup;
}
