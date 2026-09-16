package com.bosu.housebook.review;

import com.bosu.housebook.review.dto.AdminReviewResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code /api/admin/**}는 SecurityConfig에서 ROLE_ADMIN으로 이미 막혀 있어서 이 컨트롤러
 * 자체엔 별도 권한 체크가 필요 없다. */
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<AdminReviewResponse> getAll() {
        return reviewService.getAllForAdmin();
    }
}
