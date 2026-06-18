package com.dto.project.domain.admin.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {
    private Long totalMembers;
    private Long todayOrders;
    private Long pendingInquiries;
    private Long totalRevenue;
}