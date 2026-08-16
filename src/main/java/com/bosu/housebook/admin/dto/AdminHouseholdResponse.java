package com.bosu.housebook.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminHouseholdResponse(
        Long id,
        String name,
        String inviteCode,
        LocalDateTime createdAt,
        List<String> memberNames,
        long transactionCount) {
}
