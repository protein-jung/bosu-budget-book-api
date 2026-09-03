package com.bosu.housebook.featurerequest.dto;

import com.bosu.housebook.featurerequest.FeatureRequest;
import java.time.LocalDateTime;

/** 관리자 화면에서는 답변을 보낼 이메일 주소를 알아야 해서 작성자 이메일도 함께 내려준다. */
public record AdminFeatureRequestResponse(
        Long id,
        String authorName,
        String authorEmail,
        String title,
        String content,
        String adminReply,
        LocalDateTime createdAt,
        LocalDateTime repliedAt) {

    public static AdminFeatureRequestResponse from(FeatureRequest request) {
        String authorName = request.getUser() != null ? request.getUser().getName() : "탈퇴한 사용자";
        String authorEmail = request.getUser() != null ? request.getUser().getEmail() : null;
        return new AdminFeatureRequestResponse(request.getId(), authorName, authorEmail, request.getTitle(),
                request.getContent(), request.getAdminReply(), request.getCreatedAt(), request.getRepliedAt());
    }
}
