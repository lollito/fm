package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebugActionResult {
    private boolean success;
    private String message;
    private Object result;
    private String error;
    private Long snapshotId;
}
