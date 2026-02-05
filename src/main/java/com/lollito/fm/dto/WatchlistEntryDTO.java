package com.lollito.fm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lollito.fm.model.WatchlistCategory;
import com.lollito.fm.model.WatchlistPriority;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchlistEntryDTO {
    private Long id;
    private PlayerDTO player;
    private LocalDateTime addedDate;
    private String notes;
    private WatchlistPriority priority;
    private WatchlistCategory category;
    private BigDecimal addedValue;
    private BigDecimal currentValue;
    private Double addedRating;
    private Double currentRating;
    private Boolean notifyOnPerformance;
    private Boolean notifyOnTransferStatus;
    private Boolean notifyOnInjury;
    private Boolean notifyOnContractExpiry;
    private Boolean notifyOnPriceChange;
    private Integer totalNotifications;
}
