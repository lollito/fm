package com.lollito.fm.model.dto;
import com.lollito.fm.model.AdminActionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminActionFilter {
    private AdminActionType actionType;
    private String entityType;
    private String adminUsername;
}
