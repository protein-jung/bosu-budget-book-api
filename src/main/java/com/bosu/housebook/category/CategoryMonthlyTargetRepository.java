package com.bosu.housebook.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMonthlyTargetRepository extends JpaRepository<CategoryMonthlyTarget, Long> {

    Optional<CategoryMonthlyTarget> findByCategoryIdAndYearAndMonth(Long categoryId, int year, int month);

    List<CategoryMonthlyTarget> findByCategoryHouseholdIdAndYearAndMonth(Long householdId, int year, int month);

    void deleteByCategoryIdAndYearAndMonth(Long categoryId, int year, int month);
}
