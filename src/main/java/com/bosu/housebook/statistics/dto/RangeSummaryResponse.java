package com.bosu.housebook.statistics.dto;

import java.util.List;

public record RangeSummaryResponse(List<MonthlyTrendPoint> months) {
}
