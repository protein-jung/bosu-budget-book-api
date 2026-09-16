package com.bosu.housebook.review.dto;

import com.bosu.housebook.review.Review;
import java.time.LocalDateTime;

public record AdminReviewResponse(
        Long id,
        String authorName,
        String authorEmail,
        Integer rating,
        String content,
        LocalDateTime createdAt) {

    public static AdminReviewResponse from(Review review) {
        String authorName = review.getUser() != null ? review.getUser().getName() : "탈퇴한 사용자";
        String authorEmail = review.getUser() != null ? review.getUser().getEmail() : null;
        return new AdminReviewResponse(review.getId(), authorName, authorEmail, review.getRating(),
                review.getContent(), review.getCreatedAt());
    }
}
