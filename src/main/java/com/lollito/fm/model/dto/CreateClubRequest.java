package com.lollito.fm.model.dto;

import lombok.Data;

@Data
public class CreateClubRequest {
    private String name;
    private String shortName;
    private Integer foundedYear;
    private String city;
    private Long countryId;
    private Long leagueId;
    private Integer initialBudget;
    private Boolean generateInitialSquad;
    private String squadQuality;
    private CreateStadiumRequest stadiumRequest;
}
