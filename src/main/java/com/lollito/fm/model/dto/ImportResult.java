package com.lollito.fm.model.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportResult {
    private int totalRecords;
    private int successfulImports;
    private int failedImports;
    private List<String> errors;
}
