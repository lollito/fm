package com.lollito.fm.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardDTO {
    private long totalClubs;
    private long totalPlayers;
    private long totalUsers;
    private long totalLeagues;
    private long activeUsers;
    private List<AdminActionDTO> recentActions;
    private SystemHealthDTO systemHealth;
}
