package com.lollito.fm.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchlistDTO {
    private Long id;
    private String name;
    private String description;
    private List<WatchlistEntryDTO> entries;
    private Integer totalEntries;
    private Integer maxEntries;
    private LocalDateTime lastUpdated;
}
