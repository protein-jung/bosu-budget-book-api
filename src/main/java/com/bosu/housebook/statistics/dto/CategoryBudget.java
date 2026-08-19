package com.bosu.housebook.statistics.dto;

import com.bosu.housebook.common.TransactionType;
import java.math.BigDecimal;

public record CategoryBudget(
        Long categoryId,
        String categoryName,
        String color,
        String icon,
        TransactionType type,
        BigDecimal targetAmount,
        BigDecimal spentAmount) {
}
