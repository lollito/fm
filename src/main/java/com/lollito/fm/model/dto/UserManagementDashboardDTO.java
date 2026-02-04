package com.lollito.fm.model.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserManagementDashboardDTO {
    private long totalUsers;
    private long activeUsers;
    private long verifiedUsers;
    private long bannedUsers;
    private long newUsersLast24Hours;
    private long newUsersLast7Days;
    private long activeSessionsCount;
    private List<UserActivityDTO> recentActivities;
    private Map<String, Long> userRegistrationTrend;
}
