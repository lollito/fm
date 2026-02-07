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

    // Specific training bonuses
    private Double goalkeepingBonus;
    private Double defendingBonus;
    private Double attackingBonus;
    private Double fitnessBonus;
    private Double tacticalBonus;
}
