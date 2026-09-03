package com.bosu.housebook.user;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByCreatedAtAfter(LocalDateTime after);

    /** 관리자 대시보드의 회원 증가 추이 — 특정 시점까지 누적 가입자 수. */
    long countByCreatedAtLessThanEqual(LocalDateTime dateTime);
}
