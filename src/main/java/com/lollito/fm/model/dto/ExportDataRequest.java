package com.lollito.fm.model.dto;
import java.util.Map;

import com.lollito.fm.model.AdminDataType;

import lombok.Data;

@Data
public class ExportDataRequest {
    private AdminDataType dataType;
    private String format; // CSV, JSON
    private Map<String, Object> options;
}
