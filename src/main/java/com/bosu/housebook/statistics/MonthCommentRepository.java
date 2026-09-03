package com.bosu.housebook.statistics;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthCommentRepository extends JpaRepository<MonthComment, Long> {

    List<MonthComment> findByHouseholdIdAndYearAndMonthOrderByCreatedAtAsc(Long householdId, int year, int month);
}
