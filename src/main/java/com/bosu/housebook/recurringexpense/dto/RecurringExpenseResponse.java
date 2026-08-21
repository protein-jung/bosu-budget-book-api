package com.bosu.housebook.recurringexpense.dto;

import com.bosu.housebook.recurringexpense.RecurringExpense;
import java.math.BigDecimal;

public record RecurringExpenseResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryColor,
        String categoryIcon,
        String name,
        BigDecimal amount,
        int dayOfMonth,
        boolean active,
        String memo) {

    public static RecurringExpenseResponse from(RecurringExpense entity) {
        return new RecurringExpenseResponse(
                entity.getId(),
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getCategory().getColor(),
                entity.getCategory().getIcon(),
                entity.getName(),
                entity.getAmount(),
                entity.getDayOfMonth(),
                entity.isActive(),
                entity.getMemo());
    }
}
