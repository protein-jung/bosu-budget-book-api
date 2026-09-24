package com.bosu.housebook.admin.dto;

import java.util.List;

public record AdminSearchStatsResponse(long totalSearches, List<AdminSearchTermResponse> topQueries) {
}
