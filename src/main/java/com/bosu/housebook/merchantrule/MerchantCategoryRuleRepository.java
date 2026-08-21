package com.bosu.housebook.merchantrule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantCategoryRuleRepository extends JpaRepository<MerchantCategoryRule, Long> {

    List<MerchantCategoryRule> findAllByOrderBySortOrderAscIdAsc();
}
