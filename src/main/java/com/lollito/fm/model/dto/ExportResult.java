package com.lollito.fm.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportResult {
    private boolean success;
    private int recordCount;
    private String fileContent; // Base64 or URL
    private String errorMessage;
}
