package com.lollito.fm.model.dto;
import java.util.Map;

import com.lollito.fm.model.AdminDataType;

import lombok.Data;

@Data
public class ImportDataRequest {
    private AdminDataType dataType;
    private String data; // Base64 or raw string
    private Map<String, Object> options;
}
