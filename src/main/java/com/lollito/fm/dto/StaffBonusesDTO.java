package com.lollito.fm.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffBonusesDTO implements Serializable {
    private Double motivationBonus;
    private Double trainingBonus;
    private Double injuryPreventionBonus;
    private Double recoveryBonus;
    private Double scoutingBonus;
}
