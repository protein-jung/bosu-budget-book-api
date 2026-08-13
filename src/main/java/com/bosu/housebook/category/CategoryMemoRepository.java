package com.bosu.housebook.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMemoRepository extends JpaRepository<CategoryMemo, Long> {

    Optional<CategoryMemo> findByCategoryIdAndYearAndMonth(Long categoryId, int year, int month);

    List<CategoryMemo> findByCategoryHouseholdId(Long householdId);

    void deleteByCategoryIdAndYearAndMonth(Long categoryId, int year, int month);
}
