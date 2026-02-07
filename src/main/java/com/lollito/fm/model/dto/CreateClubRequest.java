package com.lollito.fm.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateClubRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String shortName;

    @NotNull(message = "Founded year is required")
    @Min(value = 1850, message = "Founded year must be after 1850")
    private Integer foundedYear;

    @NotBlank(message = "City is required")
    private String city;

    private Long countryId;

    @NotNull(message = "League ID is required")
    private Long leagueId;

    @Min(value = 0, message = "Initial budget must be positive")
    private Integer initialBudget;

    private Boolean generateInitialSquad;
    private String squadQuality;

    @Valid
    private CreateStadiumRequest stadiumRequest;
}
