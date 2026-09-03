package com.bosu.housebook.admin.dto;

import java.math.BigDecimal;

public record AdminCategoryTotalResponse(
        String categoryName,
        String categoryIcon,
        String categoryColor,
        String type,
        BigDecimal total,
        long count) {
}
