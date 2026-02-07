package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class BulkUpdatePlayersRequest {
    private List<Long> playerIds;
    private BigDecimal salaryMultiplier;
    private Double skillAdjustment;
    private Long newClubId;
}
