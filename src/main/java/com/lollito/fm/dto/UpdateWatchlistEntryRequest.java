package com.lollito.fm.dto;

import com.lollito.fm.model.WatchlistCategory;
import com.lollito.fm.model.WatchlistPriority;

import lombok.Data;

@Data
public class UpdateWatchlistEntryRequest {
    private String notes;
    private WatchlistPriority priority;
    private WatchlistCategory category;
    private Boolean notifyOnPerformance;
    private Boolean notifyOnTransferStatus;
    private Boolean notifyOnInjury;
    private Boolean notifyOnContractExpiry;
    private Boolean notifyOnPriceChange;
}
