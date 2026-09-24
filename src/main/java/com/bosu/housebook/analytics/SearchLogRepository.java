package com.bosu.housebook.analytics;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    /** 어드민 접속 통계 화면의 인기 검색어 표. */
    interface QueryStatProjection {
        String getQuery();
        Long getCount();
    }

    @Query("""
            SELECT s.query AS query, COUNT(s) AS count
            FROM SearchLog s
            WHERE s.createdAt >= :from
            GROUP BY s.query
            ORDER BY COUNT(s) DESC
            """)
    List<QueryStatProjection> findTopQueriesSince(@Param("from") LocalDateTime from, Pageable pageable);

    long countByCreatedAtGreaterThanEqual(LocalDateTime from);
}
