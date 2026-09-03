package com.bosu.housebook.merchantrule.dto;

import com.bosu.housebook.merchantrule.MerchantCategoryRule;
import java.util.List;

public record MerchantCategoryRuleResponse(Long id, Long categoryId, String categoryName, String icon, String color,
        List<String> keywords, int sortOrder) {

    public static MerchantCategoryRuleResponse from(MerchantCategoryRule rule) {
        return new MerchantCategoryRuleResponse(rule.getId(), rule.getCategory().getId(), rule.getCategory().getName(),
                rule.getCategory().getIcon(), rule.getCategory().getColor(), rule.getKeywords(), rule.getSortOrder());
    }
}
