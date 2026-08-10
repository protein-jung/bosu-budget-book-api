package com.bosu.housebook.statistics.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyTrendPoint(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netAmount,
        List<CategoryStat> byCategory,
        List<CategoryStat> byParentCategory) {
}
