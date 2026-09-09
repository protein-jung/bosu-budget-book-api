package com.bosu.housebook.transaction.dto;

import com.bosu.housebook.transaction.TransactionComment;
import java.time.LocalDateTime;

public record TransactionCommentResponse(Long id, Long userId, String authorName, String body,
        LocalDateTime createdAt) {

    public static TransactionCommentResponse from(TransactionComment comment) {
        Long userId = comment.getUser() != null ? comment.getUser().getId() : null;
        String authorName = comment.getUser() != null ? comment.getUser().getName() : "탈퇴한 사용자";
        return new TransactionCommentResponse(comment.getId(), userId, authorName, comment.getBody(),
                comment.getCreatedAt());
    }
}
