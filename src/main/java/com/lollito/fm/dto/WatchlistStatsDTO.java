package com.lollito.fm.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchlistStatsDTO {
    private Integer totalPlayers;
    private Integer availablePlayers;
    private Integer contractsExpiringSoon;
    private Integer recentlyPerformed;
    private Integer priceIncreased;
    private Integer priceDecreased;
    private BigDecimal averageValue;
    private BigDecimal totalValue;
}
