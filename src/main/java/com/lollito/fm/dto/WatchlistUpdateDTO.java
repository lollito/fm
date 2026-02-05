package com.lollito.fm.dto;

import java.time.LocalDateTime;

import com.lollito.fm.model.UpdateType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchlistUpdateDTO {
    private Long id;
    private UpdateType updateType;
    private LocalDateTime updateDate;
    private String description;
    private String previousValue;
    private String newValue;
}
