package com.lollito.fm.model.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDTO {
    private Long id;
    private String name;
    private LocalDate foundation;
    private String city;
    private String logoURL;
    private Long leagueId;
    private Long teamId;
    private Boolean isHuman;
}
