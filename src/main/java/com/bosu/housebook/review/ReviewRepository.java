package com.bosu.housebook.review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findAllByOrderByCreatedAtDesc();
}
