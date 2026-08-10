package com.bosu.housebook.statistics.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netAmount,
        List<CategoryStat> byCategory,
        List<CategoryStat> byParentCategory,
        List<CardStat> byCard,
        List<MemberStat> byMember,
        List<CategoryBudget> budgets) {
}
