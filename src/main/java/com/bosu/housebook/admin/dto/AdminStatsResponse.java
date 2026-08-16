package com.bosu.housebook.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalHouseholds,
        long totalTransactions,
        long newUsersLast7Days,
        long newHouseholdsLast7Days) {
}
