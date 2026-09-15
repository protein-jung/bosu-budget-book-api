package com.bosu.housebook.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PageViewRepository extends JpaRepository<PageView, Long> {

    /** 어드민 접속 통계 화면의 페이지별 조회수·순 방문자 표. */
    interface PathStatProjection {
        String getPath();
        Long getViews();
        Long getUniqueVisitors();
    }

    @Query("""
            SELECT p.path AS path, COUNT(p) AS views, COUNT(DISTINCT p.visitorId) AS uniqueVisitors
            FROM PageView p
            WHERE p.createdAt >= :from
            GROUP BY p.path
            ORDER BY COUNT(p) DESC
            """)
    List<PathStatProjection> findPathStatsSince(@Param("from") LocalDateTime from);

    /** 어드민 접속 통계 화면의 일별 조회수 추이 그래프. */
    interface DailyViewProjection {
        LocalDate getDay();
        Long getTotal();
    }

    @Query("""
            SELECT CAST(p.createdAt AS date) AS day, COUNT(p) AS total
            FROM PageView p
            WHERE p.createdAt >= :from
            GROUP BY CAST(p.createdAt AS date)
            """)
    List<DailyViewProjection> findDailyTotalsSince(@Param("from") LocalDateTime from);

    long countByCreatedAtGreaterThanEqual(LocalDateTime from);

    @Query("SELECT COUNT(DISTINCT p.visitorId) FROM PageView p WHERE p.createdAt >= :from")
    long countDistinctVisitorsSince(@Param("from") LocalDateTime from);
}
