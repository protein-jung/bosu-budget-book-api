package com.bosu.housebook.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminUserDetailResponse(
        Long id,
        String email,
        String name,
        LocalDate birthDate,
        boolean blocked,
        LocalDateTime createdAt,
        Long householdId,
        String householdName,
        String householdRole,
        long transactionCount,
        List<AdminTransactionResponse> recentTransactions) {
}
