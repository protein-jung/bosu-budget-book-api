package com.bosu.housebook.admin.dto;

public record AdminCampaignStatResponse(
        String utmSource,
        String utmMedium,
        String utmCampaign,
        long views,
        long uniqueVisitors) {
}
