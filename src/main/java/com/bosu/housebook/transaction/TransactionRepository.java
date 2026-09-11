package com.bosu.housebook.transaction;

import com.bosu.housebook.common.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByHouseholdIdAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
            Long householdId, LocalDate from, LocalDate to);

    List<Transaction> findByHouseholdIdAndTransactionDateBetweenAndIsBackfillFalseOrderByTransactionDateAscIdAsc(
            Long householdId, LocalDate from, LocalDate to);

    List<Transaction> findByHouseholdIdAndCardIdAndTransactionDateBetween(
            Long householdId, Long cardId, LocalDate from, LocalDate to);

    Optional<Transaction> findByIdAndHouseholdId(Long id, Long householdId);

    Optional<Transaction> findFirstByHouseholdIdOrderByTransactionDateAsc(Long householdId);

    long countByHouseholdId(Long householdId);

    long countByUserId(Long userId);

    /** 관리자 화면의 회원 상세에서 보여줄 최근 거래. */
    List<Transaction> findTop50ByUserIdOrderByTransactionDateDescIdDesc(Long userId);

    /** 헤더 검색창에서 제목(memo)/메모(note)/카테고리명으로 내역을 찾을 때 쓴다. 월별 조회와
     * 달리 기간 제한이 없어 pageable로 결과 개수를 제한한다. */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.household.id = :householdId
              AND (LOWER(t.memo) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(t.note) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(t.category.name) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<Transaction> searchByHouseholdId(@Param("householdId") Long householdId, @Param("query") String query,
            Pageable pageable);

    /** 관리자 대시보드의 전체 수입/지출 합계. */
    interface TypeTotalProjection {
        TransactionType getType();
        BigDecimal getTotal();
    }

    @Query("SELECT t.type AS type, SUM(t.amount) AS total FROM Transaction t GROUP BY t.type")
    List<TypeTotalProjection> findTotalAmountsByType();

    /** 관리자 대시보드의 일별 수입/지출 추이 그래프. */
    interface DailyTypeTotalProjection {
        LocalDate getTransactionDate();
        TransactionType getType();
        BigDecimal getTotal();
    }

    @Query("""
            SELECT t.transactionDate AS transactionDate, t.type AS type, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.transactionDate BETWEEN :from AND :to
            GROUP BY t.transactionDate, t.type
            """)
    List<DailyTypeTotalProjection> findDailyTotalsByTypeBetween(@Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** 관리자 화면에서 가계부 하나의 카테고리별 누적 합계를 보여줄 때 쓴다. */
    interface CategoryTotalProjection {
        String getCategoryName();
        String getCategoryIcon();
        String getCategoryColor();
        TransactionType getType();
        BigDecimal getTotal();
        Long getCount();
    }

    @Query("""
            SELECT c.name AS categoryName, c.icon AS categoryIcon, c.color AS categoryColor, t.type AS type,
                   SUM(t.amount) AS total, COUNT(t) AS count
            FROM Transaction t JOIN t.category c
            WHERE t.household.id = :householdId
            GROUP BY c.name, c.icon, c.color, t.type
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryTotalProjection> findCategoryTotalsByHouseholdId(@Param("householdId") Long householdId);

    /** "미분류" 카테고리(수입/지출)에 들어간 거래를 가맹점명(memo)별로 묶어 몇 건·얼마인지 센다.
     * 설정 &gt; 명세서 가져오기의 "미분류 정리" 탭에서 새 가맹점 분류 규칙을 등록할 때 쓴다. */
    interface UncategorizedMerchantProjection {
        String getMemo();
        TransactionType getType();
        Long getCount();
        BigDecimal getTotal();
    }

    @Query("""
            SELECT t.memo AS memo, t.type AS type, COUNT(t) AS count, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.household.id = :householdId AND t.category.id IN :categoryIds
              AND t.memo IS NOT NULL AND t.memo <> ''
            GROUP BY t.memo, t.type
            ORDER BY COUNT(t) DESC
            """)
    List<UncategorizedMerchantProjection> findUncategorizedMerchantSummaries(
            @Param("householdId") Long householdId, @Param("categoryIds") List<Long> categoryIds);
}
