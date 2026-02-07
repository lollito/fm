package com.lollito.fm.model.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;

@Data
public class UpdateClubRequest {
    private String name;
    private String shortName;

    @Min(value = 1850, message = "Founded year must be after 1850")
    private Integer foundedYear;

    private String city;
    private Long countryId;
    private Long leagueId;

    @Min(value = 0, message = "Initial budget must be positive")
    private Integer initialBudget;

    @Valid
    private CreateStadiumRequest stadiumRequest;
}
