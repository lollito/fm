package com.lollito.fm.model.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDTO implements Serializable {
    private Long id;
    private String name;
    private Integer startYear;
    private Integer endYear;
    private boolean current;
    private Long leagueId;
    private Integer nextRoundNumber;
}
