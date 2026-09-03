package com.bosu.housebook.featurerequest.dto;

import com.bosu.housebook.featurerequest.FeatureRequest;
import java.time.LocalDateTime;

public record FeatureRequestResponse(
        Long id,
        String authorName,
        String title,
        String content,
        String adminReply,
        LocalDateTime createdAt,
        LocalDateTime repliedAt) {

    public static FeatureRequestResponse from(FeatureRequest request) {
        String authorName = request.getUser() != null ? request.getUser().getName() : "탈퇴한 사용자";
        return new FeatureRequestResponse(request.getId(), authorName, request.getTitle(), request.getContent(),
                request.getAdminReply(), request.getCreatedAt(), request.getRepliedAt());
    }
}
