package com.bosu.housebook.category;

import com.bosu.housebook.common.TransactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByHouseholdIdOrderBySortOrderAscIdAsc(Long householdId);

    Optional<Category> findByIdAndHouseholdId(Long id, Long householdId);

    List<Category> findByHouseholdIdAndNameAndType(Long householdId, String name, TransactionType type);

    boolean existsByHouseholdIdAndParentId(Long householdId, Long parentId);

    boolean existsByHouseholdId(Long householdId);
}
