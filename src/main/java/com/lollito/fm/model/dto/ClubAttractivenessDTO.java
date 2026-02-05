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
public class ClubAttractivenessDTO implements Serializable {
    private Double leaguePositionScore;
    private Double stadiumScore;
    private Double financialScore;
    private Double historicalScore;
    private Double fanBaseScore;
    private Double overallScore;
}
