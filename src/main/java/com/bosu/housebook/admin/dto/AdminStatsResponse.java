package com.bosu.housebook.admin.dto;

import java.math.BigDecimal;

public record AdminStatsResponse(
        long totalUsers,
        long totalHouseholds,
        long totalTransactions,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        long newUsersLast7Days,
        long newHouseholdsLast7Days) {
}
