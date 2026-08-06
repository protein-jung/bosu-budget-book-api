package com.bosu.housebook.statistics.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        List<CategoryStat> byCategory,
        List<CardStat> byCard,
        List<MemberStat> byMember) {
}
