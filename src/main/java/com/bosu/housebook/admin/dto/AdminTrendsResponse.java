package com.bosu.housebook.admin.dto;

import java.util.List;

public record AdminTrendsResponse(
        List<AdminTrendPointResponse> userGrowth,
        List<AdminTrendPointResponse> dailyIncome,
        List<AdminTrendPointResponse> dailyExpense) {
}
