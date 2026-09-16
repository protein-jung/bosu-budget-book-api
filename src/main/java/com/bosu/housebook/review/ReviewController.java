package com.bosu.housebook.review;

import com.bosu.housebook.auth.CurrentUserId;
import com.bosu.housebook.review.dto.ReviewCreateRequest;
import com.bosu.housebook.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> getMine(@CurrentUserId Long userId) {
        return reviewService.getMine(userId);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@CurrentUserId Long userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(userId, request));
    }
}
