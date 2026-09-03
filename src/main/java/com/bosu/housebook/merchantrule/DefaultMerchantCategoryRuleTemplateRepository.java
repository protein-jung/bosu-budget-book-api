package com.bosu.housebook.merchantrule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefaultMerchantCategoryRuleTemplateRepository
        extends JpaRepository<DefaultMerchantCategoryRuleTemplate, Long> {

    List<DefaultMerchantCategoryRuleTemplate> findAllByOrderBySortOrderAscIdAsc();
}
