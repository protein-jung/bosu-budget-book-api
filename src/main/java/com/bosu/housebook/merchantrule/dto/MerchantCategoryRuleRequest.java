package com.bosu.housebook.merchantrule.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MerchantCategoryRuleRequest(@NotNull Long categoryId, @NotEmpty List<String> keywords) {
}
