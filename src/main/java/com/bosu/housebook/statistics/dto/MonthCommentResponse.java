package com.bosu.housebook.statistics.dto;

import com.bosu.housebook.statistics.MonthComment;
import java.time.LocalDateTime;

public record MonthCommentResponse(Long id, Long userId, String authorName, String body, LocalDateTime createdAt) {

    public static MonthCommentResponse from(MonthComment comment) {
        Long userId = comment.getUser() != null ? comment.getUser().getId() : null;
        String authorName = comment.getUser() != null ? comment.getUser().getName() : "탈퇴한 사용자";
        return new MonthCommentResponse(comment.getId(), userId, authorName, comment.getBody(), comment.getCreatedAt());
    }
}
