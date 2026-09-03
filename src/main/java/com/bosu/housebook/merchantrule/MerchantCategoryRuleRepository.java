package com.bosu.housebook.merchantrule;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantCategoryRuleRepository extends JpaRepository<MerchantCategoryRule, Long> {

    List<MerchantCategoryRule> findByHouseholdIdOrderBySortOrderAscIdAsc(Long householdId);

    Optional<MerchantCategoryRule> findByIdAndHouseholdId(Long id, Long householdId);

    boolean existsByHouseholdId(Long householdId);

    long countByHouseholdId(Long householdId);
}
