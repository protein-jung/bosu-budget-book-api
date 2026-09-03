package com.bosu.housebook.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminHouseholdDetailResponse(
        Long id,
        String name,
        String inviteCode,
        LocalDateTime createdAt,
        List<AdminMemberResponse> members,
        List<AdminCategoryTotalResponse> categoryTotals,
        long transactionCount) {
}
