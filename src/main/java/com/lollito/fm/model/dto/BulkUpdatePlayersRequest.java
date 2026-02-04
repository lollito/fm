package com.lollito.fm.model.dto;

import java.util.List;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class BulkUpdatePlayersRequest {
    private List<Long> playerIds;
    private BigDecimal salaryMultiplier;
    private Double skillAdjustment;
    private Long newClubId;
}
