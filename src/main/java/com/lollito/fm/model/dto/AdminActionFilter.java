package com.lollito.fm.model.dto;
import lombok.Builder;
import lombok.Data;
import com.lollito.fm.model.AdminActionType;

@Data
@Builder
public class AdminActionFilter {
    private AdminActionType actionType;
    private String entityType;
    private String adminUsername;
}
