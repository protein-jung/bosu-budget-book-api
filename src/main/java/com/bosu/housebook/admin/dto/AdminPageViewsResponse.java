package com.bosu.housebook.admin.dto;

import java.util.List;

public record AdminPageViewsResponse(
        long totalViews,
        long totalUniqueVisitors,
        List<AdminPageViewStatResponse> byPath,
        List<AdminCampaignStatResponse> byCampaign,
        List<AdminTrendPointResponse> daily) {
}
