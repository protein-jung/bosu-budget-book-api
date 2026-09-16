package com.bosu.housebook.review;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.review.dto.AdminReviewResponse;
import com.bosu.housebook.review.dto.ReviewCreateRequest;
import com.bosu.housebook.review.dto.ReviewResponse;
import com.bosu.housebook.user.User;
import com.bosu.housebook.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 설정 &gt; 리뷰 남기기. 개수 제한이 없어 같은 사용자가 여러 번 남긴 리뷰가 각각 별도 행으로
 * 쌓이고, 본인은 자기가 남긴 리뷰만 보고 관리자는 전체를 본다. */
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public List<ReviewResponse> getMine(Long userId) {
        return reviewRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse create(Long userId, ReviewCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        String content = request.content() != null ? request.content().trim() : null;
        Review saved = reviewRepository.save(new Review(user, request.rating(), content));
        return ReviewResponse.from(saved);
    }

    public List<AdminReviewResponse> getAllForAdmin() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminReviewResponse::from)
                .toList();
    }
}
