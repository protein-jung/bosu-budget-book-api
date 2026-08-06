package com.bosu.housebook.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByHouseholdIdOrderByIdAsc(Long householdId);

    Optional<Category> findByIdAndHouseholdId(Long id, Long householdId);
}
