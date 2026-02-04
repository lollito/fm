package com.lollito.fm.model.dto;

import com.lollito.fm.model.InjurySeverity;
import com.lollito.fm.model.InjuryType;
import lombok.Data;

@Data
public class CreateInjuryRequest {
    private InjuryType type;
    private InjurySeverity severity;
    private Integer durationDays;
}
